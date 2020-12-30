package com.cos.reflect.filter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.reflect.controller.UserController;

public class Dispatcher implements Filter {
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
//		System.out.println("컨텍스트 패스 : " + request.getContextPath());
//		System.out.println("식별자 주소 : " + request.getRequestURI());
//		System.out.println("전체 주소 : " + request.getRequestURL());
		
		String endPoint = request.getRequestURI().replaceAll(request.getContextPath(), "");
//		System.out.println("endPoint : " + endPoint);
		
		UserController userController = new UserController();
//		if (endPoint.equals("/join")) {
//			userController.join();
//		} else if (endPoint.equals("/login")) {
//			userController.login();
//		} else if (endPoint.equals("/user")) {
//			userController.user();
//		}
		Method[] methods = userController.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (endPoint.equals("/" + method.getName())) {
				try {
					method.invoke(userController);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
