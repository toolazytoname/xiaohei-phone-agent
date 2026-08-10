package io.github.toolazytoname.xiaohei;

import java.util.Arrays;

/** Deterministic automated portion of CHAT-012; human speech/TTS remains a separate gate. */
public final class ConversationAcceptanceMatrixTest {
    private static final String[] QUESTIONS = {
        "你好", "介绍一下你自己", "解释半双工", "什么是内存会话", "怎么结束聊天",
        "用一句话解释隐私", "为什么要限制轮数", "什么是 token 预算", "断网时会怎样", "如何取消请求",
        "解释模型渠道", "系统 TTS 是什么", "中转 TTS 是什么", "为什么不用模糊动作", "什么是本地 FAQ",
        "如何清空上下文", "为什么锁屏会清空", "什么是动作权限", "如何切换模型", "总结这次会话"
    };

    public static void main(String[] args) {
        twentyQuestions();
        fiveInterruptions();
        fiveTimeouts();
        fivePrivacyDenials();
        System.out.println("PASS ConversationAcceptanceMatrixTest questions=20 interruptions=5 timeouts=5 privacy_denials=5 crashes=0 automated_recorder_paths=0");
    }

    private static void twentyQuestions() {
        int accepted = 0;
        long now = 1000;
        for (int group = 0; group < 4; group++) {
            ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
            for (int offset = 0; offset < 5; offset++) {
                String question = QUESTIONS[group * 5 + offset];
                ConversationSessionCoordinator.BeginResult begin = coordinator.begin(question, "profile", now++);
                check(begin.code == ConversationSessionCoordinator.Code.REQUEST_READY, "question begin");
                ConversationPromptPolicy.Envelope envelope = ConversationPromptPolicy.build(begin.messages);
                check(envelope.messages.get(envelope.messages.size() - 1).content.equals(question), "question envelope");
                check(coordinator.complete("固定验收回复 " + accepted, now++)
                        == ConversationSessionCoordinator.Code.REPLY_ACCEPTED, "question complete");
                accepted++;
            }
        }
        check(accepted == 20, "question count");
    }

    private static void fiveInterruptions() {
        ConversationSessionCoordinator locked = active();
        check(locked.onLocked() == ConversationSessionCoordinator.Code.LOCKED_CLEARED, "lock interruption");

        ConversationSessionCoordinator background = active();
        check(background.onBackgrounded() == ConversationSessionCoordinator.Code.BACKGROUNDED_CLEARED,
            "background interruption");

        ConversationSessionCoordinator profile = active();
        check(profile.checkProfile("changed") == ConversationSessionCoordinator.Code.PROFILE_CHANGED_CLEARED,
            "profile interruption");

        ConversationSessionCoordinator cancel = active();
        check(cancel.abort(2) == ConversationSessionCoordinator.Code.REQUEST_ABORTED, "cancel interruption");

        ConversationControlPolicy.State controls = new ConversationControlPolicy.State();
        check(controls.markRequestStarted(), "stop start");
        ConversationControlPolicy.Outcome stopped = controls.apply(ConversationControlPolicy.Action.STOP);
        check(stopped.cancelRequest && stopped.modelCalls == 0, "stop interruption");
    }

    private static void fiveTimeouts() {
        ConversationSessionCoordinator beforeBegin = new ConversationSessionCoordinator(6, 2048, 1000);
        check(beforeBegin.begin("one", "p", 10).code == ConversationSessionCoordinator.Code.REQUEST_READY,
            "timeout setup 1");
        check(beforeBegin.begin("two", "p", 1010).code == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED,
            "timeout before next begin");

        ConversationSessionCoordinator lateReply = new ConversationSessionCoordinator(6, 2048, 1000);
        check(lateReply.begin("one", "p", 20).code == ConversationSessionCoordinator.Code.REQUEST_READY,
            "timeout setup 2");
        check(lateReply.complete("late", 1020) == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED,
            "late reply timeout");

        ConversationSessionCoordinator scheduled = new ConversationSessionCoordinator(6, 2048, 1000);
        check(scheduled.begin("one", "p", 30).code == ConversationSessionCoordinator.Code.REQUEST_READY,
            "timeout setup 3");
        check(scheduled.expire(1030) == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED,
            "scheduled timeout");

        ConversationSessionCoordinator rollbackClock = new ConversationSessionCoordinator(6, 2048, 1000);
        check(rollbackClock.begin("one", "p", 100).code == ConversationSessionCoordinator.Code.REQUEST_READY,
            "timeout setup 4");
        check(rollbackClock.expire(99) == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED,
            "clock rollback timeout");

        MemoryConversationSession direct = new MemoryConversationSession(6, 2048, 1000, 200);
        check(direct.beginTurn("one", 200) == MemoryConversationSession.Code.ACCEPTED, "timeout setup 5");
        check(direct.status(1200).lastCode == MemoryConversationSession.Code.TIMEOUT_CLEARED,
            "direct session timeout");
    }

    private static void fivePrivacyDenials() {
        for (String input : Arrays.asList("微信有没有未读消息", "读取我的联系人", "我现在在哪里",
                "把相册内容发给模型", "告诉我验证码")) {
            ConversationPrivacyPolicy.Result result = ConversationPrivacyPolicy.evaluate(input);
            check(result.denied && result.modelCalls == 0 && result.actionCalls == 0, "privacy denial");
        }
    }

    private static ConversationSessionCoordinator active() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        check(coordinator.begin("question", "profile", 1).code
                == ConversationSessionCoordinator.Code.REQUEST_READY, "active setup");
        return coordinator;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
