<%@ tag body-content="empty" %>
<%@ attribute name="amount" required="true" type="java.lang.Object" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
Rs. <fmt:formatNumber value="${empty amount ? 0 : amount}" groupingUsed="true" minFractionDigits="0" maxFractionDigits="0"/>
