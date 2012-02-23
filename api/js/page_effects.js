function visibleInParent(element) {
  var position = $(element).position().top
  return position >= 0 && position < $(element).offsetParent().height()
}

function hasFragment(link, fragment) {
  return $(link).attr("href").indexOf("#" + fragment) != -1
}

function findLinkByFragment(elements, fragment) {
  return $(elements).filter(function(i, e) { return hasFragment(e, fragment)}).first()
}

function setCurrentVarLink() {
  $('#vars li').removeClass('current')
  $('.public').
    filter(function(index) { return visibleInParent(this) }).
    each(function(index, element) {
      findLinkByFragment("#vars a", element.id).
        parent().
        addClass('current')
    })
}

var hasStorage = (function() { try { return localStorage.getItem } catch(e) {} }())

function scrollPositionId(element) {
  var directory = window.location.href.replace(/[^\/]+\.html$/, '')
  return 'scroll::' + $(element).attr('id') + '::' + directory
}

function storeScrollPosition(element) {
  if (!hasStorage) return;
  localStorage.setItem(scrollPositionId(element) + "::x", $(element).scrollLeft())
  localStorage.setItem(scrollPositionId(element) + "::y", $(element).scrollTop())
}

function recallScrollPosition(element) {
  if (!hasStorage) return;
  $(element).scrollLeft(localStorage.getItem(scrollPositionId(element) + "::x"))
  $(element).scrollTop(localStorage.getItem(scrollPositionId(element) + "::y"))
}

function persistScrollPosition(element) {
  recallScrollPosition(element)
  $(element).scroll(function() { storeScrollPosition(element) })
}

$(window).ready(setCurrentVarLink)
$(window).ready(function() { $('#content').scroll(setCurrentVarLink) })
$(window).ready(function() { persistScrollPosition('#namespaces')})
