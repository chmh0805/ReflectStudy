package com.cos.reflect.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cos.reflect.annotation.RequestMapping;
import com.cos.reflect.controller.UserController;

// 분기 시키기
public class Dispatcher implements Filter {
	
	private boolean isMatching = false;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		
		String endPoint = request.getRequestURI().replaceAll(request.getContextPath(), "");
		System.out.println("endPoint : " + endPoint);
		
		UserController userController = new UserController();

		// 리플렉션 --> 메서드를 런타임 시점에서 찾아내서 실행
		// getDeclaredMethods --> 그 파일에 있는 메서드만 ! (부모의 메서드 x)
		Method[] methods = userController.getClass().getDeclaredMethods();
		
		for (Method method : methods) { // 4바퀴 (join, login, user, hello)
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			RequestMapping requestMapping = (RequestMapping) annotation;
			
			if (requestMapping.value().equals(endPoint)) {
				isMatching = true;
				try {
					Parameter[] params = method.getParameters();
					String path = null;
					if (params.length != 0) {
						// 어떤 값이 들어올 지 모르기때문에 Object로 받아준다.
						// 해당 오브젝트를 리플렉션 해서 set함수 호출
						Object dtoInstance = params[0].getType().getDeclaredConstructor().newInstance();

						setData(dtoInstance, request);
						
						path = (String) method.invoke(userController, dtoInstance);
					} else {
						path = (String) method.invoke(userController);
					}
					RequestDispatcher dis = request.getRequestDispatcher(path); // 필터를 다시 안탐!
					dis.forward(request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break; // try문 안에 break X !!
			} 
		}
			
			if (isMatching == false) {
				response.setContentType("text/html; charset=utf-8");
				PrintWriter out = response.getWriter();
				out.println("잘못된 주소 요청입니다. 404 에러");
				out.flush();
			}
	}
	
	private <T> void setData(T instance, HttpServletRequest request) {
		// keys 값을 변형 username => setUsername
		// keys 값을 변형 password => setPassword
		Enumeration<String> keys = request.getParameterNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String methodKey = keyToMethodKey(key);
			
			Method[] methods = instance.getClass().getDeclaredMethods();
			
			for (Method method : methods) {
				if (method.getName().equals(methodKey)) {
					try {
						method.invoke(instance, request.getParameter(key));
					} catch (Exception e) {
						try {
							int value = Integer.parseInt(request.getParameter(key));
							method.invoke(instance, value);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					break;
				}
			}
		}
	}
	
	private String keyToMethodKey(String key) {
		String firstKey = "set";
		String upperKey = key.substring(0, 1).toUpperCase();
		String remainKey = key.substring(1);
		
		return firstKey + upperKey + remainKey;
	}
}
