package io.github.toolazytoname.xiaohei;
public final class OpenCodeTakeoverOwnershipTest { public static void main(String[] a){
 OpenCodeTakeoverOwnership o=new OpenCodeTakeoverOwnership(); String s="web-session-0001";
 expect(OpenCodeTakeoverOwnership.Decision.DENIED,o.takeOver("bad")); expect(OpenCodeTakeoverOwnership.Decision.TAKEN_OVER,o.takeOver(s)); expect(OpenCodeTakeoverOwnership.Owner.WEB,o.owner()); expect(OpenCodeTakeoverOwnership.Decision.ALREADY_OWNED,o.takeOver(s)); expect(OpenCodeTakeoverOwnership.Decision.RETURNED,o.returnToLocal(s)); expect(OpenCodeTakeoverOwnership.Owner.LOCAL,o.owner()); o.markTerminal(); expect(OpenCodeTakeoverOwnership.Decision.TERMINAL,o.takeOver(s)); System.out.println("PASS opencode-takeover transfer=1 return=1 duplicate=deny terminal=deny execution=0");}
 static void expect(Object x,Object y){if(x!=y)throw new AssertionError(String.valueOf(y));}}
