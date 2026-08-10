package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Defense-in-depth rejection of any root-shaped destructive, broad-path, or secret-bearing request. */
final class RootDestructiveDenialPolicy {
    enum Decision { DENY_DESTRUCTIVE, DENY_BROAD_PATH, DENY_SECRET, DENY_UNKNOWN }
    static Decision assess(String command, String path, String intent) {
        String c=lower(command), p=lower(path), i=lower(intent);
        if (contains(c,"rm -rf","dd if=","mkfs","wipe","reboot bootloader","fastboot flash","git reset --hard","git clean -f","iptables -f")) return Decision.DENY_DESTRUCTIVE;
        if (contains(p,"/","/data","/system","/vendor","/proc","/dev","/sdcard","../","*","~")) return Decision.DENY_BROAD_PATH;
        if (contains(c,"password","otp","token","cookie","keychain","credential","payment")||contains(i,"密码","验证码","支付","银行卡","exfiltrate","上传凭据","绕过")) return Decision.DENY_SECRET;
        return Decision.DENY_UNKNOWN;
    }
    private static String lower(String v){return v==null?"":v.toLowerCase(Locale.ROOT);}
    private static boolean contains(String value,String... terms){for(String term:terms)if(value.contains(term))return true;return false;}
}
