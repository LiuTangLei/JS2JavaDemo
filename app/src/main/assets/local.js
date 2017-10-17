//callback容器
var SDKNativeEvents = {}

/*
 * 存入传来的callback回调，加入唯一键名，使调用相同的功能可以并发
 * funcName native的方法名
 * data 参数
 * callback 回调匿名方法
 */
function sdk_launchFunc(funcName,data,callback){

    if(!data){
        alert("必须传入data");
        return;
    }

    if(!callback){
        alert("必须传入回调function");
        return;
    }
    var newName = funcName += getUniqueKey();
    SDKNativeEvents[newName] = callback;

    App.native_launchFunc(newName,JSON.stringify(data));
}

/**
 * native回调  本地做完事情后将funcName和data传过来，调用之前h5预留的匿名函数
 * funcName 对应的触发方法名
 * data 参数
 */
function sdk_nativeCallback(funcName,data){
    var obj= JSON.parse(data);
    try{
        if(SDKNativeEvents[funcName]){
            SDKNativeEvents[funcName](obj);
            SDKNativeEvents[funcName] = null;
        }
    }catch(e){
        alert(e);
    }
}

//工具--生成唯一键

var chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

function randomStr() {
    var res = "";
    for(var i = 0; i < 5; i++) {
        var id = Math.ceil(Math.random() * 35);
        res += chars[id];
    }
    return res;
}

function getUniqueKey(){
    return randomStr()+""+(new Date()).getTime();
}

window.App.pickPhotoFromLibrary = function(data, callback) {
    sdk_launchFunc("pickPhotoFromLibrary", data, callback);
}

window.App.makePhotoFromCamera = function(data, callback) {
    sdk_launchFunc("makePhotoFromCamera", data, callback);
}

window.App.encrypt = function(data, callback) {
    sdk_launchFunc("encrypt", data, callback);
}

window.App.playSnake = function(data, callback) {
    sdk_launchFunc("playSnake", data, callback);
}
