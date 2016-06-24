package org.xiaoxi.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.xiaoxi.dto.TinyurlResult;
import org.xiaoxi.dto.Url;
import org.xiaoxi.enums.DataCode;
import org.xiaoxi.enums.TinyurlStateEnum;
import org.xiaoxi.enums.UserServiceState;
import org.xiaoxi.service.TinyurlServiceInterface;
import org.xiaoxi.service.UserServiceInterface;
import org.xiaoxi.service.impl.TinyurlServiceImpl;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by YanYang on 2016/6/24.
 */
@Controller
@RequestMapping(value = "/")
public class UrlTransferContrl {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlTransferContrl.class);

    @Autowired
    private TinyurlServiceImpl tinyurlService;

    @Autowired
    private UserServiceInterface userService;

    @RequestMapping(value = "short/",
            method = RequestMethod.POST,
            produces = {"application/json;charset=utf-8"})
    @ResponseBody
    public TinyurlResult getTinyUrl(@RequestParam(value = "username")String username,
                                    @RequestParam(value = "token")String token,
                                    @RequestParam(value = "longUrl")String long_url,
                                    HttpServletRequest httpServletRequest) {

        TinyurlResult tinyurlResult = null;
        //获取域名
        String host = httpServletRequest.getHeader("host");
        try {
            boolean valid = userService.validUser(username, token);
            if (valid) {
                if (long_url == null || long_url.trim().equals("")) {
                    tinyurlResult = new TinyurlResult(false, TinyurlStateEnum.CHECK_URL.getStateInfo());
                    return tinyurlResult;
                }
                Url url = tinyurlService.transferToShort_url(long_url);
                if (url != null) {
                    //拼接完整的短网址
                    String simpleShortUrl = url.getShort_url();
                    String short_url = host + "/" + simpleShortUrl;
                    url.setShort_url(short_url);
                    tinyurlResult = new TinyurlResult<Url>(true, url, DataCode.URL.getCode(), DataCode.URL.getDesc());
                } else {
                    tinyurlResult = new TinyurlResult(false, TinyurlStateEnum.TRANSFER_FAILURE.getStateInfo());
                }
            } else {
                tinyurlResult = new TinyurlResult(false, TinyurlStateEnum.VALID_FAILURE.getStateInfo());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return tinyurlResult;
    }

    @RequestMapping(value = "{shortUrl}",
            method = RequestMethod.GET,
            produces = {"application/json;charset=utf-8"})
    @ResponseBody
    public TinyurlResult getLongUrl(@RequestParam(value = "username")String username,
                                    @RequestParam(value = "token")String token,
                                    @PathVariable(value = "shortUrl")String short_url) {
        TinyurlResult tinyurlResult = null;
        try {
            if (short_url.length() > 7) {
                tinyurlResult = new TinyurlResult(false, TinyurlStateEnum.CHECK_URL.getStateInfo());
                return tinyurlResult;
            }
            boolean valid = userService.validUser(username, token);
            if (valid) {
                Url url = tinyurlService.transferToLong_url(short_url);
                if (url != null) {
                    tinyurlResult = new TinyurlResult<Url>(true, url, DataCode.URL.getCode(), DataCode.URL.getDesc());
                } else {
                    tinyurlResult = new TinyurlResult(false, TinyurlStateEnum.TRANSFER_FAILURE.getStateInfo());
                }
            } else {
                tinyurlResult = new TinyurlResult(false, UserServiceState.FAILURE.getInfo());
                return tinyurlResult;
            }
        } catch (Exception e) {

        }
        return tinyurlResult;
    }
}