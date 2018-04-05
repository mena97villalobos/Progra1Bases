# This is an auto-generated Django model module.
# You'll have to do the following manually to clean this up:
#   * Rearrange models' order
#   * Make sure each model has one field with primary_key=True
#   * Make sure each ForeignKey has `on_delete` set to the desidered behavior.
#   * Remove `managed = False` lines if you wish to allow Django to create, modify, and delete the table
# Feel free to rename the models, but don't rename db_table values or field names.
from __future__ import unicode_literals

from django.db import models


class Administradores(models.Model):
    nombre = models.TextField(blank=True, null=True)
    alias = models.TextField(unique=True, blank=True, null=True)
    password = models.TextField(blank=True, null=True)
    percent_min = models.FloatField(blank=True, null=True)
    increment_min = models.FloatField(blank=True, null=True)  # This field type is a guess.
    imagen_default = models.BinaryField(blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'administradores'


class Aux(models.Model):
    id = models.IntegerField(primary_key=True)
    precio = models.FloatField(blank=True, null=True)  # This field type is a guess.
    fk_puja_actual = models.IntegerField(blank=True, null=True)
    alias = models.TextField(blank=True, null=True)
    fecha_fin = models.TextField(blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'aux'


class Aux2(models.Model):
    id = models.IntegerField(primary_key=True)
    precio_inicial = models.FloatField(blank=True, null=True)  # This field type is a guess.
    precio_actual = models.FloatField(blank=True, null=True)  # This field type is a guess.
    cometario_vendedor = models.TextField(blank=True, null=True)
    comentario_comprador = models.TextField(blank=True, null=True)
    alias = models.TextField(blank=True, null=True)
    fecha_fin = models.TextField(blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'aux2'


class CategoriaPrimaria(models.Model):
    nombre = models.TextField()
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'categoria_primaria'


class CategoriaSecundaria(models.Model):
    fk_categoria_primaria = models.ForeignKey(CategoriaPrimaria, models.DO_NOTHING, db_column='fk_categoria_primaria', blank=True, null=True)
    nombre = models.TextField()
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'categoria_secundaria'


class ComentariosSubastas(models.Model):
    fk_subasta = models.ForeignKey('Subastas', models.DO_NOTHING, db_column='fk_subasta', blank=True, null=True)
    fk_usuario = models.ForeignKey('Usuarios', models.DO_NOTHING, db_column='fk_usuario', blank=True, null=True)
    tipo = models.BooleanField()
    comentario = models.TextField()
    calificacion = models.FloatField()
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'comentarios_subastas'

class Items(models.Model):
    fk_categoria = models.ForeignKey(CategoriaSecundaria, models.DO_NOTHING, db_column='fk_categoria', blank=True, null=True)
    descripcion = models.TextField()
    imagen = models.BinaryField(blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'items'


class Pujas(models.Model):
    fk_comprador = models.ForeignKey('Usuarios', models.DO_NOTHING, db_column='fk_comprador', blank=True, null=True)
    monto = models.FloatField()  # This field type is a guess.
    fecha = models.DateTimeField()
    fk_subasta = models.ForeignKey('Subastas', models.DO_NOTHING, db_column='fk_subasta', blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'pujas'


class Subastas(models.Model):
    fk_item = models.ForeignKey(Items, models.DO_NOTHING, db_column='fk_item', blank=True, null=True)
    fk_puja_actual = models.ForeignKey(Pujas, models.DO_NOTHING, db_column='fk_puja_actual', blank=True, null=True)
    fk_vendedor = models.ForeignKey('Usuarios', models.DO_NOTHING, db_column='fk_vendedor', blank=True, null=False, related_name='vendedor')
    precio_inicial = models.FloatField()  # This field type is a guess.
    fecha_fin = models.DateTimeField()
    detalles_entrega = models.TextField()
    vendido = models.BooleanField()
    fk_comprador = models.ForeignKey('Usuarios', models.DO_NOTHING, db_column='fk_comprador', blank=True, null=True, related_name='comprador')
    abierta = models.NullBooleanField()
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'subastas'


class TelefonoUsuario(models.Model):
    fk_usuario = models.ForeignKey('Usuarios', models.DO_NOTHING, db_column='fk_usuario', blank=True, null=True)
    telefono = models.TextField()

    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id

    class Meta:
        managed = False
        db_table = 'telefono_usuario'


class Usuarios(models.Model):
    cedula = models.TextField(unique=True)
    nombre = models.TextField()
    apellido = models.TextField()
    alias = models.TextField(unique=True)
    password = models.TextField()
    direccion = models.TextField()
    correo = models.TextField()
    calificacion = models.FloatField(blank=True, null=True)
    
    def __unicode__(self):
        return '/%s/' % self.id

    def get_absolute_url(self):
        return '/usuario/%s' % self.id
    
    class Meta:
        managed = False
        db_table = 'usuarios'

