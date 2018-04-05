from django.shortcuts import render, redirect
from django.contrib import messages
from gestorDB import GestorDB
from django.template.response import TemplateResponse
from django.http import HttpResponseRedirect
from datetime import datetime

GestorDB('usuario')
# Create your views here.


def get_user_info_views():
    data = {}
    users = GestorDB.gestor.read_users()
    for u in users:
        key = GestorDB.gestor.get_user_info(u[0])[0]
        creadas = GestorDB.gestor.hist_subastas(u[0], False)
        ganadas = GestorDB.gestor.hist_subastas(u[0], True)
        values = [creadas, ganadas]
        data[key] = values
    return data


def get_subasta_pujas(subastas):
    dic = {}
    for subasta in subastas:
        id_subasta = int(subasta[0])
        dic[subasta] = GestorDB.gestor.hist_pujas(id_subasta)
    return dic


def usuario(request, user_id=0):
    if user_id == 0:
        try:
            user_id = request.COOKIES['user_id']
        except:
            user_id = 0
    users_info = get_user_info_views()
    cp = GestorDB.gestor.read_categoria_primaria()
    misSubastas = GestorDB.gestor.get_subastas_usuario_ganadas(user_id, False)
    misCompras = GestorDB.gestor.get_subastas_usuario_ganadas(user_id, True)
    subastasAux = GestorDB.gestor.read_subastas('', '', False)
    dicPujas = get_subasta_pujas(subastasAux)
    categoriasPrimarias = []
    categoriasSecundarias = {}
    for i in cp:
        categoriasPrimarias += [i[0]]
        cs = GestorDB.gestor.read_categoria_secundaria(i[0])
        for j in cs:
            try:
                categoriasSecundarias[i[0]] += [j[0]]
            except:
                categoriasSecundarias[i[0]] = [j[0]]
    meta_subastas = GestorDB.gestor.read_subastas_usuario(0, False)
    subastas = []
    for subasta in meta_subastas:
        subastas += [GestorDB.gestor.subastas_data(subasta)]
    try:
        return TemplateResponse(request, 'usuario/index.html', 
                                {'subastas':subastas, 
                                 'alias':request.COOKIES['user_alias'],
                                 'categoriaPrimaria':categoriasPrimarias,
                                 'categoriaSecundaria':categoriasSecundarias,
                                 'misSubastas':misSubastas,
                                 'misCompras':misCompras,
                                 'usuarios':users_info,
                                 'dicPujas':dicPujas,
                                 'year':datetime.now().year,
                                 }
                                )
    except KeyError:
        return redirect('/login')


def pujar(request):
    if request.method == "POST":
        id_subasta = int(request.get_full_path()[-1])
        id_textfield = "montoPuja"+str(id_subasta)
        monto = float(request.POST.get(id_textfield, 0))
        user_id = 0
        try:
            user_id = int(request.COOKIES['user_id'])
        except KeyError:
            return redirect('login')
        exito = GestorDB.gestor.pujar(id_subasta, user_id, monto)
        if exito:
            messages.info(request, 'Puja creada')
        else:
            messages.error(request, 'Puja Rechazada')
    return redirect('/usuario')


def login(request):
    if request.method == "POST":
        alias = str(request.POST.get('a', 0))
        password = str(request.POST.get('p', 0))
        id_user = GestorDB.gestor.validate_user(alias, password)
        if id_user != 0 and id_user is not None:
            response = usuario(request, id_user)
            response.set_cookie('user_id', id_user)
            response.set_cookie('user_alias', alias)
            return response
        else:
            return redirect('login')
    else:
        try:
            user_alias = request.COOKIES['user_alias']
            response = usuario(request)
            return response
        except:
            return TemplateResponse(request, 'usuario/login.html', {'title': 'Log in', 'year': datetime.now().year,})


def logout(request):
    response = HttpResponseRedirect('/login')
    response.delete_cookie('user_id')
    response.delete_cookie('user_alias')
    return response


def vender(request):
    id_vendedor = int(request.COOKIES['user_id'])
    precio = float(request.POST.get('precioI', 0.0))
    catP = str(request.POST.get('categoriaPrimaria', ""))
    catS = str(request.POST.get('categoriaSecundaria', ""))
    id_categoria = int(GestorDB.gestor.get_id_categoria(catP, catS))
    descrItem = str(request.POST.get('descrItem', ""))
    try:
        imgPath = request.FILES['imgInp'].read()
    except:
        imgPath = ""
    horaFin = str(request.POST.get('horaFin', ""))
    fechaFin = str(request.POST.get('fechaFin', ""))
    detEntrega = str(request.POST.get('descrEntrega', ""))
    id_item = GestorDB.gestor.crear_item(id_categoria, descrItem, imgPath)
    if id_item != 0 and id_item is not None:
        time = fechaFin + " " + horaFin
        GestorDB.gestor.crear_subasta(id_vendedor, id_item, precio, detEntrega, time)
    else:
        messages.error(request, 'Error al crear Subasta')
    return redirect('/usuario')
