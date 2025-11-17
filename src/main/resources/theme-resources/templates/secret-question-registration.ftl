<#import "template.ftl" as layout>
<@layout.registrationLayout; section>

<#if section = "header">
Secret question setup
<#elseif section = "form">
<form id="kc-secret-question-form" class="${properties.kcFormClass!}"
      action="${url.registrationAction}" method="post">

    <div class="${properties.kcFormGroupClass!}">
        <div class="${properties.kcLabelWrapperClass!}">
            <label for="secret_question" class="${properties.kcLabelClass!}">
                Select secret question</label>
        </div>
        <div class="${properties.kcInputWrapperClass!}">
            <select id="secret_question" name="secret_question" class="${properties.kcInputClass!}">
                <option value="" disabled selected>Select a question</option>
                <#if questions??>
                    <#list questions as q>
                        <option value="${q}">${q}</option>
                    </#list>
                <#else>
                    <option value="What is your favorite food?">What is your favorite food?</option>
                    <option value="What was the name of your first pet?">What was the name of your first pet?</option>
                    <option value="What city were you born in?">What city were you born in?</option>
                </#if>
            </select>
        </div>
    </div>

    <div class="${properties.kcFormGroupClass!}">
        <div class="${properties.kcLabelWrapperClass!}">
            <label for="secret_question_answer" class="${properties.kcLabelClass!}">
                Answer</label>
        </div>
        <div class="${properties.kcInputWrapperClass!}">
            <input id="secret_question_answer" name="secret_question_answer" type="text"
                   class="${properties.kcInputClass!}" autocomplete="off" required />
        </div>
    </div>

    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
               type="submit"
               value="Save" />
    </div>

</form>
</#if>
</@layout.registrationLayout>