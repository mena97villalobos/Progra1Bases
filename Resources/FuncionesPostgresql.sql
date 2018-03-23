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

CREATE OR REPLACE FUNCTION comprador_pujas(_inidpuja INTEGER)
  RETURNS TABLE(precio_inicial MONEY, oferta MONEY, comentario TEXT, calificacion REAL)
LANGUAGE plpgsql
AS $$
DECLARE
  idUsurio    NUMERIC;
  currentDate TIMESTAMP;
BEGIN
  SELECT fk_comprador
  INTO idUsurio
  FROM public.pujas
  WHERE id = _inIDpuja;
  SELECT CURRENT_DATE
  INTO currentDate;
  RETURN QUERY SELECT
                 S.precio_inicial,
                 P.monto,
                 C.comentario,
                 C.calificacion
               FROM public.subastas S
                 INNER JOIN public.pujas P ON S.fk_puja_actual = P.id
                 INNER JOIN public.comentarios_subastas C ON C.fk_subasta = S.id
               WHERE S.fk_comprador = idUsurio AND S.fecha_fin >= currentDate AND S.vendido AND (NOT C.tipo);
END;
$$;

CREATE OR REPLACE FUNCTION vendedor_subastas(_inidsubasta INTEGER)
  RETURNS TABLE(precio_inicial MONEY, oferta MONEY, comentario TEXT, calificacion REAL)
LANGUAGE plpgsql
AS $$
DECLARE
  idVendedor  NUMERIC;
  currentDate TIMESTAMP;
BEGIN
  SELECT fk_vendedor
  INTO idVendedor
  FROM subastas
  WHERE id = _inIDsubasta;
  SELECT CURRENT_DATE
  INTO currentDate;
  RETURN QUERY SELECT
                 S.precio_inicial,
                 P.monto,
                 C.comentario,
                 C.calificacion
               FROM public.subastas S
                 INNER JOIN public.pujas P ON S.fk_puja_actual = P.id
                 INNER JOIN public.comentarios_subastas C ON C.fk_subasta = S.id
               WHERE S.fk_vendedor = idVendedor AND S.fecha_fin >= currentDate AND S.vendido AND C.tipo;
END;
$$;

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
BEGIN
  INSERT INTO public.administradores (nombre, alias, password) VALUES (
    _inNombre,
    _inAlias,
    crypt(_inPassword, gen_salt('bf')) --encriptar password con blowfish algorithm
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
  INSERT INTO subastas (fk_item, fk_vendedor, precio_inicial, fecha_fin, detalles_entrega, fk_puja_actual, fk_comprador, vendido)
  VALUES (_inIDitem, _inIDvendedor, _inPrecioInicial :: FLOAT8 :: NUMERIC :: MONEY, _inFechaFin, _inDetEntrega, null, null, FALSE)
  RETURNING id)
  SELECT id INTO _outID FROM insrt;
  RETURN _outID;
END;
$$;

CREATE OR REPLACE FUNCTION listar_subastas(_inidcategoria INTEGER)
  RETURNS TABLE(descripcion TEXT, precio_inicial MONEY, puja_actual MONEY, fecha_fin DATE)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 I.descripcion,
                 S.precio_inicial,
                 P.monto,
                 S.fecha_fin
               FROM items I
                 INNER JOIN subastas S ON I.id = S.fk_item
                 INNER JOIN pujas P ON S.fk_puja_actual = P.id
               WHERE fk_categoria = _inIDcategoria
               ORDER BY S.fecha_fin DESC;
END;
$$;

CREATE OR REPLACE FUNCTION crear_puja(_inidsubasta INTEGER, _inidcomprador INTEGER, _inmonto FLOAT8)
  RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
  currentDate DATE;
  vendedor INTEGER;
  incrMin FLOAT8;
  pujaActual FLOAT8;
  fk_puja_actual INTEGER;
BEGIN
  SELECT CURRENT_TIMESTAMP INTO currentDate;
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
  ELSE
    IF (pujaActual + incrMin) < _inmonto THEN
      INSERT INTO pujas (fk_comprador, monto, fecha, fk_subasta)
      VALUES (_inIDcomprador, _inMonto :: NUMERIC :: MONEY, currentDate, _inIDsubasta);
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
  VALUES (_cedula, _nombre, _apellido, _alias, crypt(_password, gen_salt('bf')), _direccion, _correo, 0.0);
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
  RETURNS TABLE(id INTEGER, cedula TEXT, nombre TEXT, apellido TEXT, alias TEXT, direccion TEXT, correo TEXT)
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
    U.correo
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

CREATE OR REPLACE FUNCTION read_categorias()
  RETURNS TABLE(categoria_primaria TEXT, categoria_secundaria TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT
                 P.nombre,
                 S.nombre
               FROM categoria_primaria P
                 INNER JOIN categoria_secundaria S ON P.id = S.fk_categoria_primaria;
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
  RETURNS TABLE(
    idsubasta INTEGER,
    inicial DOUBLE PRECISION,
    actual DOUBLE PRECISION,
    comentariovendedor TEXT,
    comentariocomprador TEXT,
    alias TEXT,
    fecha_fin TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    recCursor CURSOR FOR
    SELECT
      s.id,
      s.precio_inicial,
      s.fk_puja_actual,
      s.fecha_fin :: TEXT,
      u.alias
    FROM
      usuarios u INNER JOIN subastas s ON u.id = s.fk_vendedor
    WHERE u.id = _inIDvendedor;
  _montoActual MONEY;
  _comentario1 TEXT;
  _comentario2 TEXT;
  rec          aux%ROWTYPE;
BEGIN
  OPEN recCursor;
  DELETE FROM aux2;
  LOOP
    FETCH recCursor INTO rec;
    IF NOT FOUND
    THEN
      EXIT;
    END IF;
    SELECT ''
    INTO _comentario1;
    SELECT ''
    INTO _comentario2;
    IF rec.fk_puja_actual NOTNULL
    THEN
      --Seleccionar el monto actual de la puja si es que existe
      SELECT p2.monto
      INTO _montoActual
      FROM (SELECT monto
            FROM pujas p
            WHERE ID = rec.fk_puja_actual) AS p2;
    ELSE
      SELECT 0.0 :: FLOAT8 :: NUMERIC :: MONEY
      INTO _montoActual; --Poner la puja actual en 0.0
    END IF;
    --Seleccionar el comentario del Vendedor al Comprador
    SELECT comentario
    INTO _comentario1
    FROM comentarios_subastas
    WHERE fk_subasta = rec.id AND tipo
    LIMIT 1;
    --Seleccionar el comentario del Comprador al Vendedor
    SELECT comentario
    INTO _comentario2
    FROM comentarios_subastas
    WHERE fk_subasta = rec.id AND NOT tipo
    LIMIT 1;
    INSERT INTO aux2 VALUES (rec.id, rec.precio, _montoActual, _comentario1, _comentario2, rec.fecha_fin, rec.alias);
  END LOOP;
  RETURN QUERY
  SELECT
    a2.id,
    a2.precio_inicial :: NUMERIC :: FLOAT8,
    a2.precio_actual :: NUMERIC :: FLOAT8,
    a2.cometario_vendedor,
    a2.comentario_comprador,
    a2.alias,
    a2.fecha_fin
  FROM aux2 a2;
END;
$$;

CREATE OR REPLACE FUNCTION hist_subastas_ganadas(_inidcomprador INTEGER)
  RETURNS TABLE(idsubasta INTEGER, inicial DOUBLE PRECISION, actual DOUBLE PRECISION, comentariovendedor TEXT, comentariocomprador TEXT, alias TEXT, fecha_fin TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    recCursor CURSOR FOR
    SELECT
      s.id,
      s.precio_inicial,
      s.fk_puja_actual,
      s.fecha_fin :: TEXT,
      u.alias
    FROM
      usuarios u INNER JOIN subastas s ON u.id = s.fk_comprador
    WHERE u.id = _inIDComprador;
  _montoActual MONEY;
  _comentario1 TEXT;
  _comentario2 TEXT;
  rec          aux%ROWTYPE;
BEGIN
  OPEN recCursor;
  DELETE FROM aux2;
  LOOP
    FETCH recCursor INTO rec;
    IF NOT FOUND
    THEN
      EXIT;
    END IF;
    SELECT ''
    INTO _comentario1;
    SELECT ''
    INTO _comentario2;
    IF rec.fk_puja_actual NOTNULL
    THEN
      --Seleccionar el monto actual de la puja si es que existe
      SELECT p2.monto
      INTO _montoActual
      FROM (SELECT monto
            FROM pujas p
            WHERE ID = rec.fk_puja_actual) AS p2;
    ELSE
      SELECT 0.0 :: FLOAT8 :: NUMERIC :: MONEY
      INTO _montoActual; --Poner la puja actual en 0.0
    END IF;
    --Seleccionar el comentario del Vendedor al Comprador
    SELECT comentario
    INTO _comentario1
    FROM comentarios_subastas
    WHERE fk_subasta = rec.id AND tipo
    LIMIT 1;
    --Seleccionar el comentario del Comprador al Vendedor
    SELECT comentario
    INTO _comentario2
    FROM comentarios_subastas
    WHERE fk_subasta = rec.id AND NOT tipo
    LIMIT 1;
    INSERT INTO aux2 VALUES (rec.id, rec.precio, _montoActual, _comentario1, _comentario2, rec.fecha_fin, rec.alias);
  END LOOP;
  RETURN QUERY
  SELECT
    a2.id,
    a2.precio_inicial :: NUMERIC :: FLOAT8,
    a2.precio_actual :: NUMERIC :: FLOAT8,
    a2.cometario_vendedor,
    a2.comentario_comprador,
    a2.alias,
    a2.fecha_fin
  FROM aux2 a2;
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
    SELECT a.increment_min:: NUMERIC :: FLOAT8 INTO _outIncrMin FROM administradores a LIMIT 1;
    END IF;

    RETURN QUERY
    SELECT _outID, _outAlias, _outPujaActual, _outDetallesEntrega, _outDescrItem, _outImagen, _outFechaFin, _outIncrMin;
  END;
$$;

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
    montoFinal FLOAT8,
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
      p.monto :: NUMERIC :: FLOAT8,
      u.calificacion :: TEXT,
      u.alias
      FROM subastas s
      INNER JOIN pujas p ON s.fk_puja_actual = p.id
      INNER JOIN usuarios u ON p.fk_comprador = _inIDuser;
  END;
$$;

DROP TRIGGER actualiza_puja ON pujas;
DROP FUNCTION  actualiza_puja();
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



