"""
Definition of urls for Progra1.
"""

from datetime import datetime
from django.conf.urls import url
import django.contrib.auth.views

import app.forms
import app.views
import usuario.views

# Uncomment the next lines to enable the admin:
from django.conf.urls import include
from django.contrib import admin
admin.autodiscover()

urlpatterns = [
    # Examples:
    url(r'^usuario/', usuario.views.usuario),
    url(r'^pujar/\d+', usuario.views.pujar),
    url(r'^$', usuario.views.login, name='login'),
    url(r'^contact$', app.views.contact, name='contact'),\
    url(r'^login$', usuario.views.login),
    url(r'^logout/', usuario.views.logout),
    url(r'^vender', usuario.views.vender),
    url(r'^admin/', include(django.contrib.admin.site.urls)),
]
