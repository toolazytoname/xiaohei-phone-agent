package io.github.toolazytoname.xiaohei;
import java.util.ArrayDeque;
/** Generation-bound sentence queue; cancellation makes all older completion events inert. */
final class SentenceTtsQueue {
 static final class Next { final long generation; final String sentence; Next(long g,String s){generation=g;sentence=s;} }
 private final ArrayDeque<String> queued=new ArrayDeque<>(); private long generation; private boolean speaking;
 Next replace(String text){ queued.clear(); speaking=false; generation++; if(text==null)return null; for(String s:text.split("(?<=[。！？.!?])")){s=s.trim();if(!s.isEmpty())queued.add(s);} return next(); }
 Next complete(long g){if(g!=generation||!speaking)return null; speaking=false;return next();}
 void cancel(){queued.clear();speaking=false;generation++;}
 int pending(){return queued.size()+(speaking?1:0);} private Next next(){if(speaking||queued.isEmpty())return null;speaking=true;return new Next(generation,queued.remove());}
}
