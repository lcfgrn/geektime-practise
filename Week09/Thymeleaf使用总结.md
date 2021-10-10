Thymeleaf使用总结

1. 如果在代码中使用@SessionAttributes("order")，后续在代码中就需要指明@ModelAttribute(name = "order")，或者将@ModelAttribute Order order作为方法的参数装配Session中的“order”属性

2. 在Java代码中可以使用Model的addAttribute方法来将参数传入html视图中

3. ${design}可以获取Model或者Session中的变量

4. *{ingredients}用来为th:object变量的属性来填充值

5. ```html
   <div th:each="ingredient : ${protein}">
       <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
       <span th:text="${ingredient.name}">INGREDIENT</span><br/>
   </div>
   ```

   可以用来遍历一个集合

6. ```html
   <span class="validationError"
         th:if="${#fields.hasErrors('name')}"
         th:errors="*{name}">Name Error</span>
   ```

这种格式的设置，然后在Java代码中的参数中加上@Valid可以用来校验参数

