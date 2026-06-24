package com.cupit.exception;

/**
 * 指定した取引コードの会員情報が存在しない場合にスローされる例外。
 */
public class MemberInfoNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MemberInfoNotFoundException(String message) {
        super(message);
    }
}
