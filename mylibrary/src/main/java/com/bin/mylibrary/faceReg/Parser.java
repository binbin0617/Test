/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.bin.mylibrary.faceReg;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceException;
}
