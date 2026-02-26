<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>画像アップロード</title>
</head>
<body>
    <h2>画像アップロード</h2>
    <form action="UploadImageServlet" method="post" enctype="multipart/form-data">
        <input type="file" name="image" accept="image/*" required>
        <input type="submit" value="アップロード">
    </form>

    <c:if test="${not empty message}">
        <p style="color:green">${message}</p>
    </c:if>
    <c:if test="${not empty error}">
        <p style="color:red">${error}</p>
    </c:if>

    <h3>アップロード済み画像一覧</h3>
    <c:if test="${not empty images}">
        <c:forEach var="img" items="${images}">
            <div style="margin-bottom:10px;">
                <img src="../menuImages/${img}" width="200">
                <p>${img}</p>
            </div>
        </c:forEach>
    </c:if>
</body>
</html>
