package io.nebulaflow.tenant;
public final class TenantContext {
  private static final ThreadLocal<String> CURRENT=new ThreadLocal<>(); private TenantContext(){}
  public static void set(String value){CURRENT.set(value);} public static String require(){String value=CURRENT.get(); if(value==null||value.isBlank()) throw new IllegalStateException("X-Tenant-Id is required"); return value;} public static void clear(){CURRENT.remove();}
}
