CREATE TABLE categoria_primaria
(
  id     SERIAL NOT NULL
    CONSTRAINT categoria_primaria_pkey
    PRIMARY KEY,
  nombre TEXT   NOT NULL
);

CREATE TABLE categoria_secundaria
(
  id                    SERIAL NOT NULL
    CONSTRAINT categoria_secundaria_pkey
    PRIMARY KEY,
  fk_categoria_primaria INTEGER
    CONSTRAINT categoria_secundaria_fk_categoria_primaria_fkey
    REFERENCES categoria_primaria,
  nombre                TEXT   NOT NULL
);

CREATE TABLE items
(
  id           SERIAL NOT NULL
    CONSTRAINT item_pkey
    PRIMARY KEY,
  fk_categoria INTEGER
    CONSTRAINT item_fk_categoria_fkey
    REFERENCES categoria_secundaria,
  descripcion  TEXT   NOT NULL,
  imagen       BYTEA
);

CREATE TABLE usuarios
(
  id           SERIAL NOT NULL
    CONSTRAINT usuarios_pkey
    PRIMARY KEY,
  cedula       TEXT   NOT NULL
    CONSTRAINT usuarios_cedula_key
    UNIQUE,
  nombre       TEXT   NOT NULL,
  apellido     TEXT   NOT NULL,
  alias        TEXT   NOT NULL
    CONSTRAINT usuarios_alias_key
    UNIQUE,
  password     TEXT   NOT NULL,
  direccion    TEXT   NOT NULL,
  correo       TEXT   NOT NULL,
  calificacion REAL
);

CREATE TABLE pujas
(
  id           SERIAL    NOT NULL
    CONSTRAINT pujas_pkey
    PRIMARY KEY,
  fk_comprador INTEGER
    CONSTRAINT pujas_fk_comprador_fkey
    REFERENCES usuarios,
  monto        MONEY     NOT NULL
    CONSTRAINT pujas_monto_check
    CHECK (monto > (0.0) :: MONEY),
  fecha        TIMESTAMP NOT NULL,
  fk_subasta   INTEGER
);

CREATE TABLE subastas
(
  id               SERIAL                       NOT NULL
    CONSTRAINT subastas_pkey
    PRIMARY KEY,
  fk_item          INTEGER
    CONSTRAINT subastas_fk_item_fkey
    REFERENCES items,
  fk_puja_actual   INTEGER
    CONSTRAINT subastas_fk_puja_actual_fkey
    REFERENCES pujas,
  fk_vendedor      INTEGER
    CONSTRAINT subastas_fk_vendedor_fkey
    REFERENCES usuarios,
  precio_inicial   MONEY DEFAULT (0.1) :: MONEY NOT NULL
    CONSTRAINT subastas_precio_inicial_check
    CHECK (precio_inicial > (0.0) :: MONEY),
  fecha_fin        TIMESTAMP                    NOT NULL,
  abierta          BOOLEAN                      NOT NULL,
  detalles_entrega TEXT                         NOT NULL,
  vendido          BOOLEAN DEFAULT FALSE        NOT NULL,
  fk_comprador     INTEGER
    CONSTRAINT subastas_fk_comprador_fkey
    REFERENCES usuarios
);

ALTER TABLE pujas
  ADD CONSTRAINT pujas_fk_subasta_fkey
FOREIGN KEY (fk_subasta) REFERENCES subastas;

CREATE TABLE comentarios_subastas
(
  id           SERIAL  NOT NULL
    CONSTRAINT comentariosubasta_pkey
    PRIMARY KEY,
  fk_subasta   INTEGER
    CONSTRAINT comentariosubasta_fk_subasta_fkey
    REFERENCES subastas,
  fk_usuario   INTEGER
    CONSTRAINT comentariosubasta_fk_usuario_fkey
    REFERENCES usuarios,
  tipo         BOOLEAN NOT NULL,
  comentario   TEXT    NOT NULL,
  calificacion REAL    NOT NULL
);

CREATE TABLE telefono_usuario
(
  id         SERIAL NOT NULL
    CONSTRAINT telefono_usuario_pkey
    PRIMARY KEY,
  fk_usuario INTEGER
    CONSTRAINT telefono_usuario_fk_usuario_fkey
    REFERENCES usuarios,
  telefono   TEXT   NOT NULL
);

CREATE TABLE administradores
(
  id             SERIAL NOT NULL
    CONSTRAINT administradores_pkey
    PRIMARY KEY,
  nombre         TEXT,
  alias          TEXT
    CONSTRAINT administradores_alias_key
    UNIQUE,
  password       TEXT,
  percent_min    REAL,
  increment_min  MONEY,
  imagen_default BYTEA
);

CREATE TABLE aux
(
  id             INTEGER,
  precio         MONEY,
  fk_puja_actual INTEGER,
  alias          TEXT,
  fecha_fin      TEXT
);

CREATE TABLE aux2
(
  id                   INTEGER,
  precio_inicial       MONEY,
  precio_actual        MONEY,
  cometario_vendedor   TEXT,
  comentario_comprador TEXT,
  alias                TEXT,
  fecha_fin            TEXT
);

CREATE EXTENSION pgcrypto;

CREATE OR REPLACE FUNCTION insertar_comentario(_inidsubasta    INTEGER, _inidusuario INTEGER, _incomentario TEXT,
                                               _incalificacion REAL, _intipo BOOLEAN)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
  calificacionActual     REAL;
  cantidadCalificaciones INTEGER;
BEGIN
  --Actualizar la calificacion al usuario
  SELECT U.calificacion
  INTO calificacionActual
  FROM usuarios U
  WHERE id = _inIDusuario;
  SELECT COUNT(*)
  INTO cantidadCalificaciones
  FROM comentarios_subastas
  WHERE fk_usuario = _inIDusuario;
  UPDATE usuarios
  SET calificacion =
  (calificacionActual * cantidadCalificaciones + _inCalificacion) / (cantidadCalificaciones + 1)
  WHERE id = _inIDusuario;
  --Insertar nueva calificacion
  INSERT INTO public.comentarios_subastas (fk_subasta, fk_usuario, tipo, comentario, calificacion)
  VALUES (_inIDsubasta, _inIDusuario, _inTipo, _inComentario, _inCalificacion);
END;
$$;

CREATE OR REPLACE FUNCTION crear_admin(_innombre TEXT, _inalias TEXT, _inpassword TEXT)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
  incr MONEY;
  perct REAL;
  ima BYTEA;
BEGIN
  SELECT a.increment_min, a.percent_min, a.imagen_default INTO incr, perct, ima FROM administradores a LIMIT 1; --Copiar Variables del sistema
  INSERT INTO public.administradores (nombre, alias, password, increment_min, percent_min, imagen_default) VALUES (
    _inNombre,
    _inAlias,
    crypt(_inPassword, gen_salt('bf')), --encriptar password con blowfish algorithm
    incr,
    perct,
    ima
  );
END;
$$;

CREATE OR REPLACE FUNCTION crear_item(_indescripcion TEXT, _inidcategoria INTEGER, _inImagen BYTEA)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _outID INTEGER;
BEGIN
  WITH insrt AS
  (INSERT INTO items (descripcion, fk_categoria, imagen) VALUES (_inDescripcion, _inIDcategoria, _inImagen) RETURNING id)
  SELECT id INTO _outID FROM insrt;
  RETURN _outID;
  EXCEPTION
  WHEN transaction_rollback THEN
    RAISE EXCEPTION 'rollback' USING errcode = '40000';
    RETURN 0;
END;
$$;

CREATE OR REPLACE FUNCTION crear_subasta(_inidvendedor INTEGER, _iniditem INTEGER, _inprecioinicial REAL,
                                         _infechafin   TIMESTAMP, _indetentrega TEXT)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _outID INTEGER;
BEGIN
  WITH insrt AS (
  INSERT INTO subastas (fk_item, fk_vendedor, precio_inicial, fecha_fin, detalles_entrega, fk_puja_actual, fk_comprador, vendido, abierta)
  VALUES (_inIDitem, _inIDvendedor, _inPrecioInicial :: FLOAT8 :: NUMERIC :: MONEY, _inFechaFin, _inDetEntrega, null, null, FALSE, TRUE)
  RETURNING id)
  SELECT id INTO _outID FROM insrt;
  RETURN _outID;
END;
$$;

CREATE OR REPLACE FUNCTION crear_puja(_inidsubasta INTEGER, _inidcomprador INTEGER, _inmonto FLOAT8)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  vendedor INTEGER;
  incrMin FLOAT8;
  pujaActual FLOAT8;
  fk_puja_actual INTEGER;
  abierta BOOLEAN;
BEGIN
  SELECT s.abierta INTO abierta FROM subastas s WHERE id = _inidsubasta;
  SELECT s.fk_vendedor INTO vendedor FROM subastas s WHERE s.id = _inidsubasta;
  SELECT p.id INTO fk_puja_actual FROM subastas s INNER JOIN pujas p ON s.fk_puja_actual = p.id WHERE s.id = _inidsubasta;
  SELECT a.increment_min :: NUMERIC :: FLOAT8 INTO incrMin FROM administradores a LIMIT 1;
  IF fk_puja_actual NOTNULL THEN
    SELECT p.monto :: NUMERIC :: FLOAT8 INTO pujaActual FROM pujas p WHERE id = fk_puja_actual;
  ELSE
    SELECT 0.0 :: NUMERIC :: FLOAT8 INTO pujaActual;
  END IF;
  IF vendedor = _inidcomprador THEN
    RETURN 0;
  ELSEIF NOT abierta THEN
    RETURN 0;
  ELSE
    IF (pujaActual + incrMin) < _inmonto THEN
      INSERT INTO pujas (fk_comprador, monto, fecha, fk_subasta)
      VALUES (_inIDcomprador, _inMonto :: NUMERIC :: MONEY, CURRENT_TIMESTAMP, _inIDsubasta);
      RETURN 1;
    ELSE
      RETURN 0;
    END IF;
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION validar_login(_inalias TEXT, _inpassword TEXT)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _outID INTEGER;
BEGIN
  SELECT U.id
  INTO _outID
  FROM usuarios U
  WHERE U.alias = _inAlias AND U.password = crypt(_inPassword, U.password);
  RETURN _outID;
END;
$$;

CREATE OR REPLACE FUNCTION validar_admin(_inalias TEXT, _inpassword TEXT)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _outID INTEGER;
BEGIN
  SELECT A.id
  INTO _outID
  FROM administradores A
  WHERE A.alias = _inAlias AND A.password = crypt(_inPassword, A.password);
  RETURN _outID;
END;
$$;

CREATE OR REPLACE FUNCTION crear_telefono(_inidusuario INTEGER, _intelefono TEXT)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO telefono_usuario (fk_usuario, telefono) VALUES (_inIDusuario, _inTelefono);
END;
$$;

CREATE OR REPLACE FUNCTION create_user(_cedula    TEXT, _nombre TEXT, _apellido TEXT, _alias TEXT, _password TEXT,
                                       _direccion TEXT, _correo TEXT)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _outIDinsertado INTEGER;
BEGIN
  INSERT INTO usuarios (cedula, nombre, apellido, alias, password, direccion, correo, calificacion)
  VALUES (_cedula, _nombre, _apellido, _alias, crypt(_password, gen_salt('bf')), _direccion, _correo, 5.0);
  SELECT id
  INTO _outIDinsertado
  FROM usuarios
  WHERE cedula = _cedula;
  RETURN _outIDinsertado;
END;
$$;

CREATE OR REPLACE FUNCTION update_variables(_ininc REAL, _inpercent REAL, _imagedef BYTEA)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE administradores
  SET increment_min = _inInc :: FLOAT8 :: NUMERIC :: MONEY, percent_min = _inPercent, imagen_default = _imageDef;
END;
$$;

CREATE OR REPLACE FUNCTION read_users()
  RETURNS TABLE(id INTEGER, nombre TEXT, alias TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 U.id,
                 U.nombre || ' ' || U.apellido,
                 U.alias
               FROM usuarios U;
END;
$$;

CREATE OR REPLACE FUNCTION read_admins()
  RETURNS TABLE(id INTEGER, nombre TEXT, alias TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 A.id,
                 A.nombre,
                 A.alias
               FROM administradores A;
END;
$$;

CREATE OR REPLACE FUNCTION get_user_info(_iniduser INTEGER)
  RETURNS TABLE(id INTEGER, cedula TEXT, nombre TEXT, apellido TEXT, alias TEXT, direccion TEXT, correo TEXT, calificacion TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    U.id,
    U.cedula,
    U.nombre,
    U.apellido,
    U.alias,
    U.direccion,
    U.correo,
    U.calificacion::TEXT
  FROM usuarios U
  WHERE U.id = _inIDuser;
END;
$$;

CREATE OR REPLACE FUNCTION update_password(_inid INTEGER, _inpass TEXT, _inadmin BOOLEAN)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  IF _inAdmin
  THEN
    UPDATE administradores
    SET password = crypt(_inPass, gen_salt('bf'))
    WHERE id = _inID;
  ELSE
    UPDATE usuarios U
    SET U.password = crypt(_inPass, gen_salt('bf'))
    WHERE U.id = _inID;
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION update_admin(_inidadmin INTEGER, _innombre TEXT, _inalias TEXT)
  RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE administradores
  SET alias = _inAlias, nombre = _inNombre
  WHERE id = _inIDadmin;
END;
$$;

CREATE FUNCTION update_usuario(_id INTEGER, _cedula TEXT, _nombre TEXT, _apellido TEXT, _alias TEXT, _direccion TEXT, _correo TEXT)
  RETURNS void AS
  $BODY$
      BEGIN
        UPDATE usuarios
        SET cedula = _cedula, nombre = _nombre, apellido = _apellido, alias = _alias,
          direccion = _direccion, correo = _correo WHERE id = _id;
      END;
  $BODY$
  LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION read_categoria_primaria()
  RETURNS TABLE(categoria_primaria TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT nombre
               FROM categoria_primaria
               ORDER BY nombre;
END;
$$;

CREATE OR REPLACE FUNCTION read_categoria_secundaria(_innombreprimaria TEXT)
  RETURNS TABLE(categoria_secundaria TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT s.nombre
               FROM categoria_secundaria s
                 INNER JOIN categoria_primaria primaria ON s.fk_categoria_primaria = primaria.id
               WHERE primaria.nombre = _inNombrePrimaria;
END;
$$;

CREATE OR REPLACE FUNCTION read_variables()
  RETURNS TABLE(incr DOUBLE PRECISION, percent REAL, image BYTEA)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 increment_min :: NUMERIC :: FLOAT8,
                 percent_min,
                 imagen_default
               FROM administradores
               LIMIT 1;
END;
$$;

CREATE OR REPLACE FUNCTION read_subastas(_incategoriaprimaria TEXT, _incategoriasecundaria TEXT, _filtro BOOLEAN)
  RETURNS TABLE(id INTEGER, nombrevendedor TEXT, fechafin TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
  _idPrimaria   INTEGER;
  _idSecundaria INTEGER;
BEGIN
  IF _filtro = 'FALSE'
  THEN
    RETURN QUERY SELECT
                   S.id,
                   U.nombre,
                   S.fecha_fin :: TEXT
                 FROM subastas S
                   INNER JOIN items ON S.fk_item = items.id
                   INNER JOIN usuarios U ON S.fk_vendedor = u.id
                 ORDER BY S.fecha_fin DESC;
  ELSE
    SELECT p.id
    INTO _idPrimaria
    FROM categoria_primaria p
    WHERE p.nombre = _inCategoriaPrimaria;
    SELECT s.id
    INTO _idSecundaria
    FROM categoria_secundaria s
    WHERE s.nombre = _inCategoriaSecundaria AND fk_categoria_primaria = _idPrimaria;
    RAISE NOTICE 'ID SECUNDARIA: %', _idSecundaria;
    RETURN QUERY SELECT
                   S.id,
                   U.nombre,
                   S.fecha_fin :: TEXT
                 FROM subastas S
                   INNER JOIN items ON S.fk_item = items.id
                   INNER JOIN usuarios U ON S.fk_vendedor = u.id
                 WHERE items.fk_categoria = _idSecundaria
                 ORDER BY S, fecha_fin DESC;
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION historial_pujas(_idsubasta INTEGER)
  RETURNS TABLE(id INTEGER, comprador TEXT, monto DOUBLE PRECISION, fecha TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 P.id,
                 U.alias,
                 P.monto :: NUMERIC :: FLOAT8,
                 P.fecha :: TEXT
               FROM pujas P
                 INNER JOIN subastas S ON S.id = _idSubasta
                 INNER JOIN usuarios u ON P.fk_comprador = u.id
               WHERE P.fk_subasta = _idSubasta
               ORDER BY P.fecha ASC;
END;
$$;

CREATE OR REPLACE FUNCTION hist_subastas_usuario(_inidvendedor INTEGER)
  RETURNS TABLE (
    idsubasta INTEGER,
    inicial DOUBLE PRECISION,
    actual DOUBLE PRECISION,
    comentariovendedor TEXT,
    comentariocomprador TEXT,
    alias TEXT,
    fecha_fin TEXT)
  LANGUAGE plpgsql
  AS $$
  BEGIN
    RETURN QUERY
    SELECT
        s.id AS idsubasta,
        s.precio_inicial :: NUMERIC :: FLOAT8 AS inicial,
        (CASE WHEN s.fk_puja_actual NOTNULL THEN (SELECT p.monto :: NUMERIC :: FLOAT8 FROM pujas p WHERE p.id = s.fk_puja_actual) ELSE  0.0 :: NUMERIC :: FLOAT8 END) AS actual,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) ELSE '' END) AS comentariovendedor,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) ELSE '' END) AS comentariocomprador,
        (CASE WHEN s.fk_comprador NOTNULL  THEN (SELECT u.alias FROM usuarios u WHERE u.id = s.fk_comprador) ELSE '' END) AS alias,
        s.fecha_fin :: TEXT AS fecha_fin
    FROM
      usuarios u INNER JOIN subastas s ON u.id = s.fk_vendedor
    WHERE u.id = _inidvendedor;
    END;
$$;

SELECT * FROM hist_subastas_usuario(6);

CREATE OR REPLACE FUNCTION hist_subastas_ganadas(_inidcomprador INTEGER)
  RETURNS TABLE (
    idsubasta INTEGER,
    inicial DOUBLE PRECISION,
    actual DOUBLE PRECISION,
    comentariovendedor TEXT,
    comentariocomprador TEXT,
    alias TEXT,
    fecha_fin TEXT)
  LANGUAGE plpgsql
  AS $$
  BEGIN
    RETURN QUERY
    SELECT
        s.id AS idsubasta,
        s.precio_inicial :: NUMERIC :: FLOAT8 AS inicial,
        (CASE WHEN s.fk_puja_actual NOTNULL THEN (SELECT p.monto :: NUMERIC :: FLOAT8 FROM pujas p WHERE p.id = s.fk_puja_actual) ELSE  0.0 :: NUMERIC :: FLOAT8 END) AS actual,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) ELSE '' END) AS comentariovendedor,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) ELSE '' END) AS comentariocomprador,
        (CASE WHEN s.fk_vendedor NOTNULL  THEN (SELECT u.alias FROM usuarios u WHERE u.id = s.fk_vendedor) ELSE '' END) AS alias,
        s.fecha_fin :: TEXT AS fecha_fin
    FROM
      usuarios u INNER JOIN subastas s ON u.id = s.fk_comprador
    WHERE u.id = _inidcomprador;
    END;
$$;

CREATE OR REPLACE FUNCTION get_id_categoria(_inprimaria TEXT, _insecundaria TEXT)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  _idPrimaria INTEGER;
  _outID      INTEGER;
BEGIN
  SELECT id
  INTO _idPrimaria
  FROM public.categoria_primaria
  WHERE nombre = _inPrimaria;
  SELECT id
  INTO _outID
  FROM categoria_secundaria
  WHERE fk_categoria_primaria = _idPrimaria AND nombre = _inSecundaria;
  RETURN _outID;
END;
$$;

CREATE OR REPLACE FUNCTION read_subastas_usuario(_inIDcategoria INTEGER, _inFiltrar BOOLEAN)
  RETURNS TABLE(id INTEGER, alias TEXT, fecha_fin TEXT, detallesItem TEXT)
  LANGUAGE plpgsql
  AS $$
  BEGIN
    IF _inFiltrar THEN
      RETURN QUERY SELECT s.id, u.alias, s.fecha_fin::TEXT, i.descripcion FROM subastas s
      INNER JOIN items i ON s.fk_item = i.id
      INNER JOIN usuarios u ON s.fk_vendedor = u.id
      WHERE s.fecha_fin > CURRENT_TIMESTAMP AND i.fk_categoria = _inIDcategoria;
    ELSE
      RETURN QUERY SELECT s.id, u.alias, s.fecha_fin::TEXT, i.descripcion FROM subastas s
      INNER JOIN items i ON s.fk_item = i.id
      INNER JOIN usuarios u ON s.fk_vendedor = u.id
      WHERE s.fecha_fin > CURRENT_TIMESTAMP;
    END IF;
  END;
$$;

CREATE OR REPLACE FUNCTION read_subasta_item(_inIDsubasta INTEGER)
  RETURNS TABLE(
    id INTEGER,
    alias TEXT,
    pujaActual FLOAT8,
    detEntrega TEXT,
    descrItem TEXT,
    imagen BYTEA,
    fechaFin TEXT,
    incrMin FLOAT8
  )
  LANGUAGE plpgsql
  AS $$
  DECLARE
    _outID INTEGER;
    _outAlias TEXT;
    _outPujaActual FLOAT8;
    _outDetallesEntrega TEXT;
    _outDescrItem TEXT;
    _outImagen BYTEA;
    _outFechaFin TEXT;
    _outIncrMin FLOAT8;
    _pujaActual INTEGER;
  BEGIN
    SELECT a.increment_min:: NUMERIC :: FLOAT8 INTO _outIncrMin FROM administradores a WHERE a.id = 1;
    SELECT s.id, s.detalles_entrega, i.descripcion, i.imagen, s.fecha_fin, u.alias, s.fk_puja_actual
    INTO _outID, _outDetallesEntrega, _outDescrItem, _outImagen, _outFechaFin, _outAlias, _pujaActual
    FROM subastas s
    INNER JOIN usuarios u ON s.fk_vendedor = u.id
    INNER JOIN items i ON s.fk_item = i.id
    WHERE s.id = _inIDsubasta;

    IF _pujaActual NOTNULL THEN
      SELECT p.monto:: NUMERIC :: FLOAT8 INTO _outPujaActual FROM subastas s
      INNER JOIN pujas p ON s.fk_puja_actual = p.id
      WHERE s.id = _inIDsubasta;
    ELSE
      SELECT s.precio_inicial:: NUMERIC :: FLOAT8 INTO _outPujaActual FROM subastas s WHERE s.id = _inIDsubasta;
    END IF;
    RETURN QUERY
    SELECT _outID, _outAlias, _outPujaActual, _outDetallesEntrega, _outDescrItem, _outImagen, _outFechaFin, _outIncrMin;
  END;
$$;

SELECT a.increment_min:: NUMERIC :: FLOAT8 FROM administradores a WHERE a.id = 1;

CREATE OR REPLACE FUNCTION get_subastas_usuario(_inIDusuario INTEGER)
  RETURNS TABLE(
    id INTEGER,
    fechaFin TEXT,
    montoActual FLOAT8,
    calificacion TEXT,
    alias TEXT
  )
  LANGUAGE plpgsql
  AS $$
  BEGIN
  RETURN QUERY
    SELECT
    s.id,
    s.fecha_fin::TEXT,
    (CASE WHEN s.fk_puja_actual ISNULL THEN 0.0 :: NUMERIC :: FLOAT8 ELSE p.monto :: NUMERIC :: FLOAT8 END),
    (CASE WHEN s.fk_puja_actual ISNULL  THEN 'No Disponible' ELSE u.calificacion::TEXT END),
    (CASE WHEN s.fk_puja_actual ISNULL  THEN 'No Disponible' ELSE u.alias::TEXT END)
    FROM subastas s
    LEFT JOIN pujas p ON s.fk_puja_actual = p.id
    LEFT JOIN usuarios u ON p.fk_comprador = u.id
    WHERE s.fk_vendedor = _inIDusuario;
  END;
$$;

CREATE OR REPLACE FUNCTION get_subastas_ganadas(_inIDuser INTEGER)
  RETURNS TABLE(
    id INTEGER,
    fechaFin TEXT,
    montoActual FLOAT8,
    calificacion TEXT,
    alias TEXT
  )
  LANGUAGE plpgsql
  AS $$
  BEGIN
    RETURN QUERY
      SELECT
        s.id,
        s.fecha_fin::TEXT,
        p.monto::NUMERIC::FLOAT8,
        u.calificacion::TEXT,
        u.alias
      FROM subastas s
      INNER JOIN pujas p ON s.fk_puja_actual = p.id
      INNER JOIN usuarios u ON s.fk_vendedor = u.id
      WHERE s.fk_comprador = _inIDuser;
  END;
$$;

CREATE FUNCTION actualiza_puja()
  RETURNS TRIGGER AS
  $BODY$
      BEGIN
        UPDATE subastas SET fk_puja_actual = NEW.id WHERE id = NEW.fk_subasta;
        RETURN NEW;
      END;
  $BODY$
  LANGUAGE 'plpgsql';

CREATE TRIGGER actualiza_puja AFTER INSERT
  ON pujas
  FOR EACH ROW EXECUTE PROCEDURE actualiza_puja();

CREATE FUNCTION get_fk_vendedor(idSubasta INTEGER)
  RETURNS INTEGER
  LANGUAGE plpgsql
  AS $$
  DECLARE
    outID INTEGER;
  BEGIN
    SELECT s.fk_vendedor INTO outID FROM subastas s WHERE s.id = idSubasta;
    RETURN outID;
  END;
$$;

CREATE OR REPLACE FUNCTION get_tel_usuario(_inIDuser INTEGER)
  RETURNS TABLE(
  tel TEXT
  )
  LANGUAGE plpgsql
  AS $$
  BEGIN
    RETURN QUERY SELECT tu.telefono FROM telefono_usuario tu WHERE tu.fk_usuario = _inIDuser;
  END;
$$;

CREATE ROLE iniciar_sesion WITH ENCRYPTED PASSWORD '9545';
GRANT EXECUTE ON FUNCTION validar_admin(TEXT, TEXT) TO iniciar_sesion;
GRANT EXECUTE ON FUNCTION validar_user(TEXT, TEXT) TO iniciar_sesion;
GRANT SELECT ON administradores TO iniciar_sesion;
GRANT SELECT ON usuarios TO iniciar_sesion;

CREATE ROLE admin WITH ENCRYPTED PASSWORD '9545';
ALTER ROLE admin WITH LOGIN;
GRANT EXECUTE ON FUNCTION
  crear_admin(TEXT, TEXT, TEXT),
  crear_item(TEXT, INTEGER, BYTEA),
  crear_telefono(INTEGER, TEXT),
  create_user(TEXT, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT),
  get_id_categoria(TEXT, TEXT),
  get_tel_usuario(INTEGER),
  get_user_info(INTEGER),
  hist_subastas_ganadas(INTEGER),
  hist_subastas_usuario(INTEGER),
  historial_pujas(INTEGER),
  read_admins(),
  read_categoria_primaria(),
  read_categoria_secundaria(TEXT),
  read_subastas(TEXT, TEXT, BOOLEAN),
  read_subastas_usuario(INTEGER, BOOLEAN),
  read_users(),
  read_variables(),
  update_usuario(INTEGER, TEXT, TEXT, TEXT, TEXT, TEXT, TEXT),
  update_admin(INTEGER, TEXT, TEXT),
  update_password(INTEGER, TEXT, BOOLEAN),
  update_variables(REAL, REAL, BYTEA)
TO admin;
GRANT SELECT ON TABLE
  administradores,
  aux,
  aux2,
  categoria_primaria,
  categoria_secundaria,
  comentarios_subastas,
  items,
  pujas,
  subastas,
  telefono_usuario,
  usuarios
TO admin;
GRANT INSERT ON TABLE
  administradores,
  aux,
  aux2,
  telefono_usuario,
  usuarios
TO admin;
GRANT ALL PRIVILEGES ON SEQUENCE
  administradores_id_seq,
  telefono_usuario_id_seq,
  usuarios_id_seq
TO admin;
GRANT UPDATE ON TABLE
  telefono_usuario,
  usuarios,
  administradores
TO admin;

CREATE ROLE usuario WITH ENCRYPTED PASSWORD '9545';
ALTER ROLE usuario WITH LOGIN;
GRANT EXECUTE ON FUNCTION
  read_categoria_primaria(),
  read_categoria_secundaria(TEXT),
  read_subastas(TEXT, TEXT, BOOLEAN),
  read_subasta_item(INTEGER),
  read_subastas_usuario(INTEGER, BOOLEAN),
  read_users(),
  read_variables(),
  get_id_categoria(TEXT, TEXT),
  get_tel_usuario(INTEGER),
  get_fk_vendedor(INTEGER),
  get_subastas_ganadas(INTEGER),
  get_subastas_usuario(INTEGER),
  get_user_info(INTEGER),
  hist_subastas_ganadas(INTEGER),
  hist_subastas_usuario(INTEGER),
  historial_pujas(INTEGER),
  insertar_comentario(INTEGER, INTEGER, TEXT, REAL, BOOLEAN),
  crear_item(TEXT, INTEGER, BYTEA),
  crear_puja(INTEGER, INTEGER, FLOAT8),
  crear_subasta(INTEGER, INTEGER, REAL, TIMESTAMP, TEXT)
TO usuario;
GRANT SELECT ON TABLE
administradores,
  aux,
  aux2,
  categoria_primaria,
  categoria_secundaria,
  comentarios_subastas,
  items,
  pujas,
  subastas,
  telefono_usuario,
  usuarios
TO usuario;
GRANT INSERT, UPDATE ON TABLE
  comentarios_subastas,
  items,
  pujas,
  subastas,
  aux,
  aux2
TO usuario;
GRANT ALL PRIVILEGES ON SEQUENCE
  comentarios_subastas_id_seq,
  items_id_seq,
  pujas_id_seq,
  subastas_id_seq
TO usuario;
GRANT DELETE ON TABLE
  aux,
  aux2
TO usuario, admin;

CREATE OR REPLACE FUNCTION calendarizador()
  RETURNS VOID
  LANGUAGE plpgsql
  AS $$
  DECLARE
	contador INTEGER := 1;
	maxKey INTEGER;
  pujaActual INTEGER := NULL;
  fkComprador INTEGER;
BEGIN
	SELECT MAX(s.id) INTO maxKey FROM subastas s;
	UPDATE subastas SET abierta = FALSE WHERE fecha_fin <= CURRENT_TIMESTAMP;
	UPDATE subastas SET vendido = TRUE WHERE fk_puja_actual NOTNULL;
	LOOP
	EXIT WHEN contador > maxKey;
    SELECT s.fk_puja_actual INTO pujaActual FROM subastas s WHERE s.id = contador;
		IF pujaActual NOTNULL THEN
      SELECT p.fk_comprador INTO fkComprador FROM pujas p WHERE p.id = pujaActual;
      UPDATE subastas SET fk_comprador = fkComprador WHERE id = contador;
    END IF;
	  contador := contador+1;
  END LOOP;
END;
$$;

CREATE OR REPLACE FUNCTION pruebas(_inID INTEGER)
  RETURNS TABLE (
    idsubasta INTEGER,
    inicial DOUBLE PRECISION,
    actual DOUBLE PRECISION,
    comentariovendedor TEXT,
    comentariocomprador TEXT,
    alias TEXT,
    fecha_fin TEXT)
  LANGUAGE plpgsql
  AS $$
  BEGIN
    RETURN QUERY
    SELECT
        s.id AS idsubasta,
        s.precio_inicial :: NUMERIC :: FLOAT8 AS inicial,
        (CASE WHEN s.fk_puja_actual NOTNULL THEN (SELECT p.monto :: NUMERIC :: FLOAT8 FROM pujas p WHERE p.id = s.fk_puja_actual) ELSE  0.0 :: NUMERIC :: FLOAT8 END) AS actual,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND cs.tipo) ELSE '' END) AS comentariovendedor,
        (CASE WHEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) NOTNULL
          THEN (SELECT comentario FROM comentarios_subastas cs WHERE cs.fk_subasta = s.id AND NOT cs.tipo) ELSE '' END) AS comentariocomprador,
       u.alias AS alias,--comprador
      s.fecha_fin :: TEXT AS fecha_fin
    FROM
      usuarios u INNER JOIN subastas s ON u.id = s.fk_comprador
    WHERE u.id = _inID;
    END;
$$;

CREATE OR REPLACE FUNCTION get_alias(user_id INTEGER)
  RETURNS TEXT
  LANGUAGE plpgsql
  AS $$
    DECLARE
      alias_return TEXT;
    BEGIN
      SELECT u.alias INTO alias_return FROM usuarios u WHERE u.id = user_id;
      RETURN alias_return;
    END;
$$;
