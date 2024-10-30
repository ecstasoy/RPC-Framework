package org.example.rpc.core.router.impl;

import org.example.rpc.core.common.annotations.*;
import org.example.rpc.core.model.RpcRequest;
import org.example.rpc.core.router.api.Router;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default router.
 *
 * <p>It is used to route the request to the target service.
 *
 * @see Router
 * @author Kunhua Huang
 */
@Component
public class DefaultRouter implements Router {

  private String basePath;
  private String methodPath;

  private static boolean hasHttpMethodAnnotation(Method method, String httpMethod) {
    switch (httpMethod) {
      case "GET":
        return method.isAnnotationPresent(GET.class);
      case "POST":
        return method.isAnnotationPresent(POST.class);
      case "PUT":
        return method.isAnnotationPresent(PUT.class);
      case "DELETE":
        return method.isAnnotationPresent(DELETE.class);
      default:
        return false;
    }
  }

  @Override
  public Method route(Class<?> serviceClass, String methodName, String httpMethod) {
    for (Method method : serviceClass.getDeclaredMethods()) {
      if (method.getName().equals(methodName) && hasHttpMethodAnnotation(method, httpMethod)) {
        return method;
      }
    }

    return null;
  }

  @Override
  public Object[] resolveParameters(Method method, RpcRequest request) {
    // Get the base path from the class-level @Api annotation
    basePath = "";
    Api api = method.getDeclaringClass().getAnnotation(Api.class);
    if (api != null) {
      basePath = api.value();
    }

    // Get the method path from the method-level annotation
    methodPath = "";
    if (method.isAnnotationPresent(GET.class)) {
      methodPath = method.getAnnotation(GET.class).value();
    } else if (method.isAnnotationPresent(POST.class)) {
      methodPath = method.getAnnotation(POST.class).value();
    } else if (method.isAnnotationPresent(PUT.class)) {
      methodPath = method.getAnnotation(PUT.class).value();
    } else if (method.isAnnotationPresent(DELETE.class)) {
      methodPath = method.getAnnotation(DELETE.class).value();
    }

    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];
    Map<String, Object> paramMap = request.getParameters();
    Map<String, String> queryParams = request.getQueryParams();

    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];
      if (param.isAnnotationPresent(Path.class)) {
        String pathParamName = param.getAnnotation(Path.class).value();
        String value = extractPathParameter(request.getPath(), pathParamName);
        args[i] = convertParameter(value, param.getType(), "path");
      } else if (param.isAnnotationPresent(Query.class)) {
        String queryParamName = param.getAnnotation(Query.class).value();
        args[i] = convertParameter(queryParams.get(queryParamName), param.getType(), "query");
      } else if (param.isAnnotationPresent(Body.class)) {
        args[i] = paramMap.get("body");
      } else {
        args[i] = paramMap.get(param.getName());
      }
    }
    return args;
  }

  private String extractPathParameter(String path, String paramName) {
    // remove leading slash
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    String fullTemplate = (basePath + methodPath).replaceAll("^/+", "");

    // Convert the full template to a regex pattern
    String regex = fullTemplate.replaceAll("\\{([^/]+)\\}", "([^/]+)");
    Pattern pattern = Pattern.compile(regex);

    // Extract parameter names
    List<String> paramNames = new ArrayList<>();
    Matcher paramMatcher = Pattern.compile("\\{([^/]+)\\}").matcher(fullTemplate);
    while (paramMatcher.find()) {
      paramNames.add(paramMatcher.group(1));
    }

    // Match the path with the pattern
    Matcher matcher = pattern.matcher(path);
    if (matcher.matches()) {
      int paramIndex = paramNames.indexOf(paramName);
      if (paramIndex >= 0 && paramIndex < matcher.groupCount()) {
        return matcher.group(paramIndex + 1);
      }
    }

    throw new IllegalArgumentException("Path parameter " + paramName + " not found in path: " + path);
  }

  private Object convertParameter(String value, Class<?> targetType, String type) {
    if (value == null) {
      return null;
    }

    if (targetType == String.class) {
      return value;
    }

    if (targetType == Integer.class || targetType == int.class) {
      return Integer.parseInt(value);
    }

    if (targetType == Long.class || targetType == long.class) {
      return Long.parseLong(value);
    }

    if (targetType == Boolean.class || targetType == boolean.class) {
      return Boolean.parseBoolean(value);
    }

    throw new IllegalArgumentException("Unsupported {} parameter type: " + type);
  }
}
