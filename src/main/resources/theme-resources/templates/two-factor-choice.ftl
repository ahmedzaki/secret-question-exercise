<#import "template.ftl" as layout>
<@layout.registrationLayout; section>

<#if section = "header">
Secret question verification
<#elseif section = "form">

<form id="kc-totp-login-form" class="${properties.kcFormClass!}"
      action="${url.loginAction}" method="post">
    <h3>Please select secondary authentication method:</h3>
        <#if methods??>
            <#list methods?keys as key>
                <button type="submit" name="chosen_method" value="${key}">${methods[key]}</button>
            </#list>
        <#else>
            <button type="submit" name="chosen_method" value="otp">Use One-Time Password (OTP)</button>
        </#if>
</form>
</#if>
</@layout.registrationLayout>