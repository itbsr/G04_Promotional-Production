<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8" />
		<title>管理者ログイン画面</title>
			<link
			rel="stylesheet"
			type="text/css"
			href="<c:url value='/assets/css/AdminLogin.css'/>"
		/>
	</head>
	<body>
		<h1>管理者ログイン画面</h1>
		<form action="AdminLoginServlet" method="post">
			<table>
				<tr>
					<td>管理者ID</td>
					<td><input type="text" name="username" /></td>
				</tr>
				<tr>
					<td>パスワード</td>
					<td><input type="password" name="password" /></td>
				</tr>
				<tr>
					<td colspan="2"><input type="submit" value="ログイン" /></td>
				</tr>
			</table>
		</form>
		<c:if test="${not empty errorMessage}">
			<div class="error-container">
				<p>${errorMessage}</p>
			</div>
		</c:if>
	</body>
</html>
