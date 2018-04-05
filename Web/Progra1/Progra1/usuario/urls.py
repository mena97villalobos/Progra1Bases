from django.conf.urls import patterns, url, include

urlpatterns = ['',
    url(r'^$', 'usuario.views.usuario', name='usuario'),
    url(r'^pujar/\d+', 'usuario.views.pujar', name='pujar'),
    url(r'^logout', 'usuario.views.logout', name='logout'),
    url(r'^vender', 'usuario.view.vender', name='vender'),
    ]
