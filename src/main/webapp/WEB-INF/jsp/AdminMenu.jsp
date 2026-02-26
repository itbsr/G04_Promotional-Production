<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8" />
		<title>管理機能選択画面</title>
		<link rel="stylesheet" type="text/css" href="<c:url value='/assets/css/AdminMenu.css'/>" />

		<!-- PWA Settings -->
		<link rel="manifest" href="manifest.json" />
		<meta name="theme-color" content="#1e293b" />
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-status-bar-style" content="default" />
		<meta name="apple-mobile-web-app-title" content="G04 Manage" />
	</head>
	<body>
		<h1>管理機能選択画面</h1>
		<ul>
			<li><a href="sales">売上管理</a></li>
			<li><a href="ManageMenuServlet">管理メニュー</a></li>
			<li><a href="UploadImageServlet">メニュー画像登録</a></li>
			<li><a href="devices">デバイス登録</a></li>
			<li><a href="AdminUserRegisterServlet">管理者登録</a></li>
			<!-- 他の管理機能へのリンクをここに追加 -->
		</ul>
	</body>
</html>
