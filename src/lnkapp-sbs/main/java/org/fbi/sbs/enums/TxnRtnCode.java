package org.fbi.sbs.enums;

import java.util.Hashtable;

/**
 * 业务交易返回码
 */
public enum TxnRtnCode implements EnumApp {

    TXN_EXECUTE_SECCESS("0000", "交易完成"),
    TXN_EXECUTE_FAILED("1000", "交易失败"),

    UNKNOWN_EXCEPTION("9000", "其它未知异常");

    private String code = null;
    private String title = null;
    private static Hashtable<String, TxnRtnCode> aliasEnums;

    TxnRtnCode(String code, String title) {
        this.init(code, title);
    }

    @SuppressWarnings("unchecked")
    private void init(String code, String title) {
        this.code = code;
        this.title = title;
        synchronized (this.getClass()) {
            if (aliasEnums == null) {
                aliasEnums = new Hashtable();
            }
        }
        aliasEnums.put(code, this);
        aliasEnums.put(title, this);
    }

    public static TxnRtnCode valueOfAlias(String alias) {
        return aliasEnums.get(alias);
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public String toRtnMsg() {
        return this.code + "|" + this.title;
    }
}
