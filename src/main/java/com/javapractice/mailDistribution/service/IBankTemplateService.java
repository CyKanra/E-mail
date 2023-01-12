package com.javapractice.mailDistribution.service;

import java.util.List;

/**
 * @InterfaceName: IBankTemplateService
 * @Description:
 * @Author: Kanra
 * @Date: 2022/09/13
 */
public interface IBankTemplateService {
    
    /**
     * @Description:
     * @param:
     * @return:
     * @throws:
     * @date: 2022/09/13
     */
    String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML,
                             String[] newHtml, Long[] id, Long accountId, Long scVersion);
}
