from psycopg2 import connect
import datetime, os, base64

class GestorDB:
    conn = None
    cur = None
    gestor = None

    def __init__(self, user):
        GestorDB.conn = self.get_connection(user)
        GestorDB.gestor = self

    @staticmethod
    def get_connection(user):
        return connect("dbname=Progra1 user=" + user + " password=9545")

    # Returns id
    def validate_user(self, user, password):
        cur = self.conn.cursor()
        cur.callproc("validar_login", (user, password))
        user_id = cur.fetchone()[0]
        if user_id != 0 and user_id is not None:
            GestorDB.conn = self.get_connection("usuario")
        cur.close()
        return user_id

    # Returns [(id, nombre, alias)]
    def read_users(self):
        cur = self.conn.cursor()
        cur.callproc("read_users", ())
        return cur.fetchall()

    # Returns [(id, cedula, nombre, apellido, alias, direccion, correo, calificacion)]
    def get_user_info(self, user_id):
        cur = self.conn.cursor()
        cur.callproc("get_user_info", (user_id, ))
        return cur.fetchall()

    # Returns [(nombre, )]
    def read_categoria_primaria(self):
        cur = self.conn.cursor()
        cur.callproc("read_categoria_primaria", ())
        return cur.fetchall()

    # Returns [(nombre, )]
    def read_categoria_secundaria(self, primaria):
        cur = self.conn.cursor()
        cur.callproc("read_categoria_secundaria", (primaria, ))
        return cur.fetchall()

    # Returns [(id, nombreVendedor, fechaFin)]
    def read_subastas(self, primaria, secundaria, filtro):
        cur = self.conn.cursor()
        cur.callproc("read_subastas", (primaria, secundaria, filtro))
        return cur.fetchall()

    # Returns [(idsubasta, inicial, actual, comentariovendedor, comentariocomprador, alias, fecha_fin)]
    def hist_subastas(self, id_user, ganadas):
        cur = self.conn.cursor()
        if ganadas:
            cur.callproc("hist_subastas_ganadas", (id_user, ))
        else:
            cur.callproc("hist_subastas_usuario", (id_user,))
        return cur.fetchall()

    # id_subasta TIENE QUE SER STRING
    # Returns [(id, comprador, fecha, monto)]
    def hist_pujas(self, id_subasta):
        cur = self.conn.cursor()
        cur.callproc("historial_pujas", (id_subasta, ))
        return cur.fetchall()

    # Returns int
    def get_id_categoria(self, primaria, secundaria):
        cur = self.conn.cursor()
        cur.callproc("get_id_categoria", (primaria, secundaria))
        id_categoria = cur.fetchone()[0]
        if id_categoria is None:
            id_categoria = 0
        cur.close()
        return id_categoria

    # Return id
    def crear_item(self, id_categoria, desc, b):
        cur = self.conn.cursor()
        """
        with open(image_path, "rb") as imageFile:
            f = imageFile.read()
            b = bytearray(f)
            imageFile.close()
        """
        cur.callproc("crear_item", (desc, id_categoria, b))
        self.conn.commit()
        return cur.fetchone()[0]

    def crear_subasta(self, id_vendedor, id_item, precio, d_entrega, time):
        # mm dd yyyy HH:MM
        ts = datetime.datetime.strptime(time, '%m %d %Y %H:%M')
        cur = self.conn.cursor()
        cur.callproc("crear_subasta", (id_vendedor, id_item, precio, ts, d_entrega))
        self.conn.commit()
        return cur.fetchone()[0]

    def read_subastas_usuario(self, id_categoria, filtrar):
        cur = self.conn.cursor()
        cur.callproc("read_subastas_usuario", (id_categoria, filtrar))
        return cur.fetchall()

    # Returns (id, alias, pujaActual, detEntrega, descrItem, imagen, fechaFin, incrMin)
    def read_subastas_item(self, id_subasta):
        cur = self.conn.cursor()
        cur.callproc("read_subasta_item", (id_subasta, ))
        return cur.fetchone()

    def pujar(self, id_subasta, id_comprador, monto):
        cur = self.conn.cursor()
        cur.callproc("crear_puja", (id_subasta, id_comprador, monto))
        self.conn.commit()
        return cur.fetchone()[0]

    def get_subastas_usuario_ganadas(self, id_user, ganadas):
        cur = self.conn.cursor()
        if ganadas:
            cur.callproc("get_subastas_ganadas", (id_user, ))
        else:
            cur.callproc("get_subastas_usuario", (id_user, ))
        return cur.fetchall()

    def get_fk_vendedor(self, id_subasta):
        cur = self.conn.cursor()
        cur.callproc("get_fk_vendedor", (id_subasta, ))
        return cur.fetchone()[0]

    def get_telefonos(self, id_user):
        cur = self.conn.cursor()
        cur.callproc("get_tel_usuario", (id_user,))
        return cur.fetchall()

    def get_alias(self, id_user):
        cur = self.conn.cursor()
        cur.callproc("get_alias", (id_user,))
        return cur.fetchone()[0]

    # Returns (id, alias, pujaActual, detEntrega, descrItem, imagen, fechaFin, incrMin)
    def subastas_data(self, tupla):
        id_subasta = tupla[0]
        data = self.gestor.read_subastas_item(id_subasta)
        base64EncodedStr = base64.b64encode(data[5])

        d = (data[0], data[1], data[2], data[3], data[4], base64EncodedStr, data[6], data[7])
        return d

# GestorDB("usuario")
# path = "C:/Users/mena9/Desktop/Bases2/Progra1/Progra1Bases/Resources/default.png"
# id_itemOut = GestorDB.gestor.crear_item(19, "insertado desde python", path)
# print(GestorDB.gestor.crear_subasta(1, id_itemOut, 7800.0, "Subasta desde python"))
# print(GestorDB.gestor.pujar(7, 6, 80000.45))
# print(GestorDB.gestor.read_subastas_item(7))

