package com.bin.mylibrary.interfaces;

import java.util.List;

/**
 * 权限回调接口
 */
public interface PermissionListener {
    //授权成功
    void onGranted();

    //授权部分
    void onGranted(List<String> grantedPermission);

    //拒绝授权
    void onDenied(List<String> deniedPermission);
}
