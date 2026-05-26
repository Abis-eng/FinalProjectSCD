<%@ tag body-content="empty" %>
<%@ attribute name="amount" required="true" %>
<%@ page import="com.elcinic.utility.MoneyUtil" %>
<%= MoneyUtil.format(amount) %>
