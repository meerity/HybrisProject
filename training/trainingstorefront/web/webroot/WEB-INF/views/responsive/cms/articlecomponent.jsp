<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="article-component">
    <div class="article-image">
        <img src="${component.image.url}" alt="${component.image.altText}"
             style="float: left; margin: 0 20px 20px 0; max-width: 600px; max-height: 600px;"/>
    </div>
    <div class="article-text">
        ${component.text}
    </div>
</div>