package com.creditville.notifications.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * Created by Chuks on 02/06/2021.
 */
@Slf4j
@Aspect
@Component
public class MonitoredScheduleAspect {
    @Before("execution(* com.creditville.notifications.jobs.NotificationJobs.*(..))")
    public void beforeScheduleBegins(JoinPoint joinPoint) {
        System.out.print("before schedule begins <><><>");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.info(String.format("%sjob has started. Start time: %tc", this.getFormattedMethodName(method.getName()), new Date()).toUpperCase());
    }

    @After("execution(* com.creditville.notifications.jobs.NotificationJobs.*(..))")
    public void afterScheduleBegins(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.info(String.format("%sjob has successfully completed it's operation. End time: %tc ", this.getFormattedMethodName(method.getName()), new Date()).toUpperCase());
    }

    private String getFormattedMethodName(String methodName) {
        StringBuilder sb = new StringBuilder();
        if(!this.containsUpperCase(methodName)) return methodName + " ";
        for (String w : methodName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
            sb.append(w).append(" ");
        return sb.toString();
    }

    private boolean containsUpperCase(String str) {
        char ch;
        boolean exists = false;
        for(int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            if (Character.isUpperCase(ch))
                exists = true;
        }
        return exists;
    }
}
