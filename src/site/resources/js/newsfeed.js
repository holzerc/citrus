$(document).ready(function () {
    $('#newsfeed').rssfeed('http://labs.consol.de/tags/citrus/feed/', {
      header: false,
      snippet: false,
      limit: 5
    });
});