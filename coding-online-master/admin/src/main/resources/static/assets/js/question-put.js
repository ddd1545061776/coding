var question = {}
var questionTypes = [];

$(document).ready(function () {
    $('#question-description').summernote({
        height: 150
    });

    $.ajax({
        url: baseUrl + '/question-type',
        type: 'post',
        contentType: 'application/json',
        dataType: "json",
        success: function (res) {
            questionTypes = res;
            let $questionType = $('#question-type');
            for (let i = 0; i < questionTypes.length; i++) {
                $questionType.append('<option>' + questionTypes[i] + '</option>>');
            }
        },
        error: function (res) {
        }
    });
})

$(document).ready(function () {
    $('#question-description').summernote({
        height: 150
    });

    $.ajax({
        url: baseUrl + '/input-type',
        type: 'post',
        contentType: 'application/json',
        dataType: "json",
        success: function (res) {
            questionTypes = res;
            let $questionType = $('#input_type');
            let $questionType1 = $('#input_type1');
            for (let i = 0; i < questionTypes.length; i++) {
                $questionType.append('<option>' + questionTypes[i] + '</option>>');
                $questionType1.append('<option>' + questionTypes[i] + '</option>>');
            }
        },
        error: function (res) {
        }
    });
})


$('#input-add').click(function () {
    let input = $('#input-mode').clone();
    input.removeAttr('style');
    input.find('input').val("");
    $('#output-0').before(input);
})

$('#output-add').click(function () {
    let output = $('#output-mode').clone();
    output.removeAttr('style');
    output.find('input').val("");
    $('#submit-div').before(output);
})

function deleteThis(attr) {
    $(attr).parent().remove();
}

$('#question-submit').click(function () {
    question.title = $('#question-title').val();
    question.difficulty = $('#question-difficulty').val();
    question.type = $('#question-type').val();
    question.description = $('#question-description').val();
    question.input = $('#question-input').val();
    question.output = $('#question-output').val();
    question.time = $('#question-time').val();
    question.memory = $('#question-memory').val();
    question.inputType = $('#input_type').val();
    question.inputType1 = $('#input_type1').val();
    question.inputExample = [];
    $('.input-example').each(function () {
        let inputValue = $(this).find('input').val().trim();
        if (inputValue.length != 0) {
            question.inputExample.push(inputValue);
        }
    });
    question.outputExample = [];
    $('.output-example').each(function () {
        let outputValue = $(this).find('input').val().trim();
        if (outputValue.length != 0) {
            question.outputExample.push(outputValue);
        }
    });

    let params = JSON.stringify(question);

    $.ajax({
        url: baseUrl + '/question-submit',
        type: 'post',
        data: params,
        contentType: 'application/json',
        dataType: "json",
        success: function (res) {
            swal("上传成功！","提示", "success").then(function () {
                $(location).attr('href', baseUrl + '/question-list');
            });
        },
        error: function (res) {
            let data = jQuery.parseJSON(res.responseText);
            swal(data.message, "上传失败", "error").then(function () {
                window.location.reload()
            });
        }
    })
})
