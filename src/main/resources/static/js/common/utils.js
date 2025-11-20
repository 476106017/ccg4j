/**
 * 数组去重并统计数量
 * @param {Array} arr 
 * @returns {Array}
 */
var distinctArr = function(arr){
    return Object.entries(
        arr.reduce((count, el) => ((count[el] = ++count[el] || 1), count), {})
      ).map(([el, count]) => `${el}${count > 1 ? count.toString() : ""}`);
}

/**
 * 对象转字符串显示
 * @param {Object} obj 
 * @returns {String}
 */
var dictShow = function(obj){
    let show = "";
    for (let key in obj) {
        if(key.indexOf("_")<0 && obj[key]>0){
            show = show+key+":"+obj[key]+"\n";
        }
    }
    return show;
}

/**
 * 获取URL参数
 * @param {String} name 
 * @returns {String|null}
 */
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}
