/**
 * 
 */

console.log("this is script file");

$(".bar").css("display","none");
$(".sidebar_mini").css("display","none");

const toggleSidebar=()=>{
    if($(".sidebar").is(":visible")){
        //if true then hide sidebar
        $(".sidebar").css("display","none");
        $(".sidebar_mini").css("display","block");
        $(".content").css("margin-left","5%");
        $(".bar").css("display","block");
        $(".cross").css("display","none");
    }
    else{
        //else show hide bar
        $(".sidebar_mini").css("display","none");
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
        $(".bar").css("display","none");
        $(".cross").css("display","block");

    }
};