package io.github.toolazytoname.xiaohei;
public final class ToolGatewayTest { public static void main(String[] x) {
 ToolGateway g=new ToolGateway(); ToolGateway.Token t=new ToolGateway.Token("token-000000000001","task-0001","android.open_gallery",2000,true);
 ok(g.authorize("task-0001","android.open_gallery",ToolCatalog.Risk.LOW,"key-0000000000001",t,1000),ToolGateway.Decision.ALLOW);
 ok(g.authorize("task-0001","android.open_gallery",ToolCatalog.Risk.LOW,"key-0000000000002",t,1000),ToolGateway.Decision.TOKEN_REPLAY);
 ToolGateway.Token u=new ToolGateway.Token("token-000000000002","task-0001","android.observe",999,true);
 ok(g.authorize("task-0001","android.observe",ToolCatalog.Risk.OBSERVE,"key-0000000000003",u,1000),ToolGateway.Decision.TOKEN_EXPIRED);
 ok(g.authorize("task-0002","android.open_gallery",ToolCatalog.Risk.LOW,"key-4",t,1000),ToolGateway.Decision.TOKEN_SCOPE);
 System.out.println("PASS tool-gateway token_scope=deny replay=deny expiry=deny"); }
 static void ok(Object a,Object b){if(a!=b)throw new AssertionError(a+" != "+b);} }
