package com.javapractice.mailDistribution.service;

/**
 * @InterfaceName: HttpMail
 * @Description:http方法でメールを取得とメールの配付ルールを設置
 * @Author: Kanra
 * @Date: 2022/09/20
 */
public interface HttpMail {

    /**
     * @Description:
     * @param:
     * @return:
     * @throws:
     * @date: 2022/09/20
     */
    void httpMailCatach(String userUrl, String password, String forwardMail, String key, String accountid_phoneId);
}
