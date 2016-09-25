package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rkulkarni on 7/19/16.
 */
public class JunitParallelRunnerPrintStream extends PrintStream{
  private static PrintStream consolePrintStream = null;
  public JunitParallelRunnerPrintStream() {
    super(System.out);
    consolePrintStream = System.out;
  }

  private ConcurrentHashMap<String, PrintStream> map = new ConcurrentHashMap<String, PrintStream>();

  private PrintStream getPrintStream() {
    String currentThreadName = Thread.currentThread().getName();
    if(map.containsKey(currentThreadName)) {
      return map.get(currentThreadName);
    } else {
      try {
        if("main".equalsIgnoreCase(currentThreadName)) {
         return consolePrintStream;
        } else {
          consolePrintStream.println("Opening printstream to file " + currentThreadName + ".txt");
          PrintStream p = new PrintStream(currentThreadName + ".txt");
          map.put(currentThreadName, p);
          return p;
        }
      } catch (FileNotFoundException e) {
        return System.out;
      }
    }
  }

  public PrintStream	append(char c){
    return getPrintStream().append(c);
  }
  public PrintStream	append(CharSequence csq) {
    return getPrintStream().append(csq);
  }
  public PrintStream	append(CharSequence csq, int start, int end) {
    return getPrintStream().append(csq,start,end);
  }
  public boolean	checkError() {
    return getPrintStream().checkError();
  }
  public void	clearError() {

  }
  public void	close() {
    getPrintStream().close();
  }
  public void	flush() {
    getPrintStream().flush();
  }

  public PrintStream	format(Locale l, String format, Object... args) {
    return getPrintStream().format(l,format,args);
  }
  public PrintStream	format(String format, Object... args) {
    return getPrintStream().format(format, args);
  }
  public void	print(boolean b) {
    getPrintStream().print(b);
  }
  public void	print(char c) {
    getPrintStream().print(c);
  }
  public void	print(char[] s) {
    getPrintStream().print(s);
  }
  public void	print(double d) {
    getPrintStream().print(d);
  }
  public void	print(float f) {
    getPrintStream().print(f);
  }
  public void	print(int i) {
    getPrintStream().print(i);
  }
  public void	print(long l) {
    getPrintStream().print(l);
  }
  public void	print(Object obj) {
    getPrintStream().print(obj);
  }
  public void	print(String s) {
    getPrintStream().print(s);
  }
  public PrintStream	printf(Locale l, String format, Object... args) {
    return getPrintStream().printf(l, format, args);
  }
  public PrintStream	printf(String format, Object... args) {
    return getPrintStream().printf(format, args);
  }
  public void	println() {
    getPrintStream().println();
  }
  public void	println(boolean x) {
    getPrintStream().println();
  }
  public void	println(char x) {
    getPrintStream().println(x);
  }
  public void	println(char[] x) {
    getPrintStream().println(x);
  }
  public void	println(double x) {
    getPrintStream().println(x);
  }
  public void	println(float x) {
    getPrintStream().println(x);
  }
  public void	println(int x) {
    getPrintStream().println(x);
  }
  public void	println(long x) {
    getPrintStream().println(x);
  }
  public void	println(Object x) {
    getPrintStream().println(x);
  }
  public void	println(String x) {
    getPrintStream().println(x);
  }
  public void	setError() {
  }
  public void	write(byte[] buf, int off, int len) {
    getPrintStream().write(buf, off, len);
  }
  public void	write(int b) {
    getPrintStream().write(b);
  }
  public void	write(byte[] b) {
    try {
      getPrintStream().write(b);
    } catch (IOException e) {

    }
  }

}
