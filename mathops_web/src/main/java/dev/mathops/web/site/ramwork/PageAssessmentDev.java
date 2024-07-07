package dev.mathops.web.site.ramwork;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

enum PageAssessmentDev {
    ;

    /**
     * Generates the page.
     *
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final String title = Res.get(Res.SITE_TITLE);
        Page.startEmptyPage(htm, title, true);

        htm.sH(1).add("Assessment Development").eH(1);

        htm.addln("<style>");
        htm.addln("  div.expression {");
        htm.addln("    display:inline-block;");
        htm.addln("    min-width:300px;");
        htm.addln("    padding:3px;");
        htm.addln("    min-width: 200px;");
        htm.addln("    min-height: 30px;");
        htm.addln("    background-color: #f3f3f3;");
        htm.addln("    border: 1px solid black;");
        htm.addln("    font-size:30px;");
        htm.addln("  }");
        htm.addln("  div.expression:focus {");
        htm.addln("    background-color: #fffff0;");
        htm.addln("    outline: 1px solid #ccccff;");
        htm.addln("  }");
        htm.addln("  div.mrow {");
        htm.addln("    display: inline;");
        htm.addln("    vertical-align:middle;");
        htm.addln("  }");
        htm.addln("  div.fraction {");
        htm.addln("    margin-left:5px;");
        htm.addln("    margin-right:5px;");
        htm.addln("    display: inline-block;");
        htm.addln("    vertical-align:middle;");
        htm.addln("  }");
        htm.addln("  div.numer {");
        htm.addln("    display: block;");
        htm.addln("    border-bottom:2px solid navy;");
        htm.addln("    padding-left:5px;");
        htm.addln("    padding-right:5px;");
        htm.addln("    padding-bottom:3px;");
        htm.addln("    min-width:30px;");
        htm.addln("    text-align:center;");
        htm.addln("  }");
        htm.addln("  div.denom {");
        htm.addln("    display: block;");
        htm.addln("    padding-left:5px;");
        htm.addln("    padding-right:5px;");
        htm.addln("    min-width:10px;");
        htm.addln("    text-align:center;");
        htm.addln("  }");
        htm.addln("  div.parenthesized {");
        htm.addln("    display: inline-block;");
        htm.addln("    background: #f1f1ff;");
        htm.addln("    vertical-align:baseline;");
        htm.addln("    border-left:2px black solid;");
        htm.addln("    border-right:2px black solid;");
        htm.addln("    border-radius:5px/15px;");
        htm.addln("    margin-left:3px;");
        htm.addln("    margin-right:3px;");
        htm.addln("    padding-left:3px;");
        htm.addln("    padding-right:3px;");
        htm.addln("  }");
        htm.addln("</style>");

        htm.addln("<p><div class='expression' id='div123' tabindex='0'>",
                "<img width='0' height='22' style='outline:1px solid red;'></img></div></p>");
        htm.addln("<input type='button' name='parenbtn' id='parenbtn' value='( )'/>");
        htm.addln("<input type='button' name='fractionbtn' id='fractionbtn' value='Fraction'/>");
        htm.addln("<input type='button' name='exponentbtn' id='exponentbtn' value='Exponent'/>");
        htm.addln("<select id='functionselect' name='functionselect' onChange='insert_function(this)'>");
        htm.addln("  <option>Function...</option>");
        htm.addln("  <option>sin()</option>");
        htm.addln("  <option>cos()</option>");
        htm.addln("  <option>tan()</option>");
        htm.addln("  <option>exp()</option>");
        htm.addln("  <option>ln()</option>");
        htm.addln("</select>");

        htm.addln("<p> Generated text:</p>");
        htm.addln("<p><input id='text123' type='text' width='30' readonly='true'/></p>");

        htm.addln("<script>");
        htm.addln("  var divElem;");
        htm.addln("  var textElem;");
        htm.addln("  var expression=[];");
        htm.addln("  var cursor=0;");
        htm.addln("  var fractionbtn;");
        htm.addln("  var exponentbtn;");
        htm.addln("  var functionselect;");

        htm.addln("  function expr_keydown(event) {");
        htm.addln("    if (event.isComposing || event.keyCode === 229) {");
        htm.addln("      return;");
        htm.addln("    }");

        htm.addln("    const code = event.which;");

        htm.addln("    var change = false;");

        htm.addln("    if (event.shiftKey) {");
        htm.addln("      if (code == 187) { // + (shift of '=')");
        htm.addln("        expression.splice(cursor, 0, '+');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 56) { // * (shift of '8')");
        htm.addln("        expression.splice(cursor, 0, '*');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 57) { // ( (shift of '9')");
        htm.addln("        insert_parentheses();");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 48) { // ) (shift of '0')");
        htm.addln("        // Jump out of current parentheses, if any");
        htm.addln("        let i = cursor;");
        htm.addln("        while (i < expression.length) {");
        htm.addln("          if (expression[i] == '[close-paren]') {");
        htm.addln("            cursor = i + 1;");
        htm.addln("            change = true;");
        htm.addln("            break;");
        htm.addln("          }");
        htm.addln("          ++i;");
        htm.addln("        }");
        htm.addln("      }");
        htm.addln("    } else {");
        htm.addln("      if (code == 8) { // backspace");
        htm.addln("        if (cursor > 0) {");
        htm.addln("          // FIXME: Deletion of a construction");
        htm.addln("          cursor--;");
        htm.addln("          expression.splice(cursor, 1);");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 35) { // end");
        htm.addln("        if (cursor != expression.length) {");
        htm.addln("          cursor = expression.length;");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 36) { // home");
        htm.addln("        if (cursor > 0) {");
        htm.addln("          cursor = 0;");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 37) { // left arrow");
        htm.addln("        if (cursor > 0) {");
        htm.addln("          cursor--;");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 39) { // right arrow");
        htm.addln("        if (cursor < expression.length) {");
        htm.addln("          cursor++;");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 46) { // delete");
        htm.addln("        if (cursor < expression.length) {");
        htm.addln("          // FIXME: Deletion of a construction");
        htm.addln("          expression.splice(cursor, 1);");
        htm.addln("          change = true;");
        htm.addln("        }");
        htm.addln("      } else if (code == 48 || code == 96) { // 0");
        htm.addln("        expression.splice(cursor, 0, '0');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 49 || code == 97) { // 1");
        htm.addln("        expression.splice(cursor, 0, '1');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 50 || code == 98) { // 2");
        htm.addln("        expression.splice(cursor, 0, '2');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 51 || code == 99) { // 3");
        htm.addln("        expression.splice(cursor, 0, '3');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 52 || code == 100) { // 4");
        htm.addln("        expression.splice(cursor, 0, '4');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 53 || code == 101) { // 5");
        htm.addln("        expression.splice(cursor, 0, '5');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 54 || code == 102) { // 6");
        htm.addln("        expression.splice(cursor, 0, '6');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 55 || code == 103) { // 7");
        htm.addln("        expression.splice(cursor, 0, '7');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 56 || code == 104) { // 8");
        htm.addln("        expression.splice(cursor, 0, '8');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 57 || code == 105) { // 9");
        htm.addln("        expression.splice(cursor, 0, '9');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 110 || code == 190) { // .");
        htm.addln("        expression.splice(cursor, 0, '.');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 107) { // +");
        htm.addln("        expression.splice(cursor, 0, "+");");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 109 || code == 189) { // -");
        htm.addln("        expression.splice(cursor, 0, '-');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 106) { // *");
        htm.addln("        expression.splice(cursor, 0, '*');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      } else if (code == 111 || code == 191) { // slash (divided by)");
        htm.addln("        expression.splice(cursor, 0, '/');");
        htm.addln("        ++cursor;");
        htm.addln("        change = true;");
        htm.addln("      }");
        htm.addln("    }");

        htm.addln("    console.log(\"'Code is ' + code + ', change is ' + change\");");

        htm.addln("    if (change) {");
        htm.addln("      rebuild();");
        htm.addln("    }");
        htm.addln("  }");

        htm.addln("  function rebuild() {");
        htm.addln("    var text = '';");
        htm.addln("    var html = \"<div class='mrow'/>\";");
        htm.addln("    for (i in expression) {");
        htm.addln("      if (i == cursor) {");
        htm.addln("        html = html + \"<img width='0' height='22' style='outline:1px solid red;'></img>\";");
        htm.addln("      }");
        htm.addln("      var item = expression[i];");

        htm.addln("      if ('[start-fraction]' == item) {");
        htm.addln("        text = text + '\\frac{';");
        htm.addln("        html = html + \"</div><div class='fraction'><div class='numer'>\";");
        htm.addln("      } else if ('[mid-fraction]' == item) {");
        htm.addln("        text = text + '}{';");
        htm.addln("        html = html + \"</div><div class='denom'>\";");
        htm.addln("      } else if ('[end-fraction]' == item) {");
        htm.addln("        text = text + '}';");
        htm.addln("        html = html + \"</div></div><div class='mrow'/>\";");
        htm.addln("      } else if ('[start-exponent]' == item) {");
        htm.addln("        text = text + '^{';");
        htm.addln("        html = html + '</div><sup><div class='mrow'/>';");
        htm.addln("      } else if ('[end-exponent]' == item) {");
        htm.addln("        text = text + '}'");
        htm.addln("        html = html + \"</div></sup><div class='mrow'/>\";");
        htm.addln("      } else if ('[start-fxn-sin]' == item) {");
        htm.addln("        text = text + '\\sin{';");
        htm.addln("        html = html + ' sin(';");
        htm.addln("      } else if ('[start-fxn-cos]' == item) {");
        htm.addln("        text = text + '\\cos{';");
        htm.addln("        html = html + ' cos(';");
        htm.addln("      } else if ('[start-fxn-tan]' == item) {");
        htm.addln("        text = text + '\\tan{';");
        htm.addln("        html = html + ' tan(';");
        htm.addln("      } else if ('[start-fxn-exp]' == item) {");
        htm.addln("        text = text + '\\exp{';");
        htm.addln("        html = html + ' exp(';");
        htm.addln("      } else if ('[start-fxn-ln]' == item) {");
        htm.addln("        text = text + '\\ln{';");
        htm.addln("        html = html + ' ln(';");
        htm.addln("      } else if ('[end-fxn]' == item) {");
        htm.addln("        text = text + '}';");
        htm.addln("        html = html + ') ';");
        htm.addln("      } else if ('+' == item) {");
        htm.addln("        text = text + '+';");
        htm.addln("        html = html + ' + ';");
        htm.addln("      } else if ('-' == item) {");
        htm.addln("        text = text + '-';");
        htm.addln("        html = html + ' &minus; ';");
        htm.addln("      } else if ('*' == item) {");
        htm.addln("        text = text + '*';");
        htm.addln("        html = html + ' &times; ';");
        htm.addln("      } else if ('/' == item) {");
        htm.addln("        text = text + '/';");
        htm.addln("        html = html + ' &div; ';");
        htm.addln("      } else if ('[open-paren]' == item) {");
        htm.addln("        text = text + '(';");
        htm.addln("        html = html + \"<div class='parenthesized'><div class='mrow'/>\";");
        htm.addln("      } else if ('[close-paren]' == item) {");
        htm.addln("        text = text + ')';");
        htm.addln("        html = html + '</div></div>';");
        htm.addln("      } else {");
        htm.addln("        text = text + item;");
        htm.addln("        html = html + item;");
        htm.addln("      }");
        htm.addln("    }");
        htm.addln("    if (cursor == expression.length) {");
        htm.addln("      html = html + \"<img width='0' height='22' style='outline:1px solid red;'></img>\";");
        htm.addln("    }");
        htm.addln("    html = html + '</div>'; // mrow");
        htm.addln("    textElem.value=text;");
        htm.addln("    divElem.innerHTML=html");
        htm.addln("  }");

        htm.addln("  function insert_fraction() {");
        htm.addln("    expression.splice(cursor, 0, '[start-fraction]', '[mid-fraction]', '[end-fraction]');");
        htm.addln("    ++cursor;");
        htm.addln("    divElem.focus();");
        htm.addln("    rebuild();");
        htm.addln("  }");

        htm.addln("  function insert_exponent() {");
        htm.addln("    expression.splice(cursor, 0, '[start-exponent]', '[end-exponent]');");
        htm.addln("    ++cursor;");
        htm.addln("    divElem.focus();");
        htm.addln("    rebuild();");
        htm.addln("  }");

        htm.addln("  function insert_parentheses() {");
        htm.addln("    expression.splice(cursor, 0, '[open-paren]', '[close-paren]');");
        htm.addln("    ++cursor;");
        htm.addln("    divElem.focus();");
        htm.addln("    rebuild();");
        htm.addln("  }");

        htm.addln("  function insert_function(obj) {");
        htm.addln("    var value = obj.value;");

        htm.addln("    if ('sin()' == value) {");
        htm.addln("      expression.splice(cursor, 0, '[start-fxn-sin]', '[end-fxn]');");
        htm.addln("      ++cursor;");
        htm.addln("      divElem.focus();");
        htm.addln("      rebuild();");
        htm.addln("    } else if ('cos()' == value) {");
        htm.addln("      expression.splice(cursor, 0, '[start-fxn-cos]', '[end-fxn]');");
        htm.addln("      ++cursor;");
        htm.addln("      divElem.focus();");
        htm.addln("      rebuild();");
        htm.addln("    } else if ('tan()' == value) {");
        htm.addln("      expression.splice(cursor, 0, '[start-fxn-tan]', '[end-fxn]');");
        htm.addln("      ++cursor;");
        htm.addln("      divElem.focus();");
        htm.addln("      rebuild();");
        htm.addln("    } else if ('exp()' == value) {");
        htm.addln("      expression.splice(cursor, 0, '[start-fxn-exp]', '[end-fxn]');");
        htm.addln("      ++cursor;");
        htm.addln("      divElem.focus();");
        htm.addln("      rebuild();");
        htm.addln("    } else if ('ln()' == value) {");
        htm.addln("      expression.splice(cursor, 0, '[start-fxn-ln]', '[end-fxn]');");
        htm.addln("      ++cursor;");
        htm.addln("      divElem.focus();");
        htm.addln("      rebuild();");
        htm.addln("    }");

        htm.addln("    obj.value='Function...'");
        htm.addln("  }");

        htm.addln("  window.addEventListener('load', (event) => {");
        htm.addln("    divElem = document.getElementById('div123');");
        htm.addln("    divElem.addEventListener('keydown', (event) => {expr_keydown(event);});");

        htm.addln("    textElem = document.getElementById('text123');");

        htm.addln("    arenbtn = document.getElementById('parenbtn');");
        htm.addln("    arenbtn.addEventListener('click', () => {insert_parentheses();});");

        htm.addln("    fractionbtn = document.getElementById('fractionbtn');");
        htm.addln("    fractionbtn.addEventListener('click', () => {insert_fraction();});");

        htm.addln("    exponentbtn = document.getElementById('exponentbtn');");
        htm.addln("    exponentbtn.addEventListener('click', () => {insert_exponent();});");

        htm.addln("    functionselect = document.getElementById('functionselect');");
        htm.addln("    functionselect.addEventListener('change', (obj) => {insert_function(obj);});");
        htm.addln("  });");
        htm.addln("</script>");

        htm.eDiv(); // maincontent
        Page.endEmptyPage(htm, true);
        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
}
