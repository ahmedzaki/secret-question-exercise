<#import "template.ftl" as layout>
<@layout.registrationLayout; section>

<#if section = "header">
Secret question verification
<#elseif section = "form">

<form id="kc-totp-login-form" class="${properties.kcFormClass!}"
      action="${url.loginAction}" method="post" xmlns="http://www.w3.org/1999/html">
    <div class="${properties.kcFormGroupClass!}">
        <div class="${properties.kcLabelWrapperClass!}">
            <label for="secret_question_answer" class="${properties.kcLabelClass!}">
                ${loginSecretQuestion}
            </label>
            <input id="secret_question" name="secret_question" type="text" hidden="true"
                   value="${loginSecretQuestion}"/>
        </div>
        <div class="${properties.kcInputWrapperClass!}">
            <input id="secret_question_answer" name="secret_question_answer" type="text"
                   class="${properties.kcInputClass!}"/>
        </div>
    </div>

    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
               type="submit"
               value="Continue"/>

        <input style="margin-top=1rem;"
               class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
               name="action" type="submit" value="fallback">
        Other verification options
        </input>
    </div>

</form>
</#if>
</@layout.registrationLayout>