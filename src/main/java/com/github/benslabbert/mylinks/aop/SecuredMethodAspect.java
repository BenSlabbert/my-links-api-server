package com.github.benslabbert.mylinks.aop;

import static com.github.benslabbert.mylinks.util.Encoder.encodeUUID;

import com.github.benslabbert.mylinks.exception.UnauthorizedException;
import com.github.benslabbert.mylinks.factory.StorageServiceFactory;
import com.github.benslabbert.mylinks.handler.CustomHeaders;
import com.github.benslabbert.mylinks.service.StorageService;
import io.netty.handler.codec.http.HttpMessage;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SecuredMethodAspect {

  private final UnauthorizedException unauthorizedException =
      new UnauthorizedException("unable to authenticate request");

  private final StorageService storageService;

  public SecuredMethodAspect() {
    storageService = StorageServiceFactory.INSTANCE;
  }

  @Pointcut("@annotation(secured)")
  public void callAt(Secured secured) {
    // this is a method pointcut for any method using this annotation
  }

  @Around(value = "callAt(secured)", argNames = "pjp,secured")
  public Object around(ProceedingJoinPoint pjp, Secured secured) throws Throwable {
    var httpMessage =
        Arrays.stream(pjp.getArgs())
            .filter(HttpMessage.class::isInstance)
            .map(HttpMessage.class::cast)
            .findFirst();

    if (httpMessage.isEmpty()) {
      throw unauthorizedException;
    }

    var userId = encodeUUID(getHeader(httpMessage.get(), CustomHeaders.USER_ID));
    var token = encodeUUID(getHeader(httpMessage.get(), CustomHeaders.TOKEN));

    var storedToken = storageService.getToken(userId);
    if (storedToken.isEmpty()) {
      throw unauthorizedException;
    }

    if (!storedToken.get().equals(token)) {
      throw unauthorizedException;
    }

    return pjp.proceed();
  }

  private String getHeader(HttpMessage httpMessage, CustomHeaders header) {
    if (!httpMessage.headers().contains(header.val())) {
      throw unauthorizedException;
    }

    return httpMessage.headers().get(header.val());
  }
}
