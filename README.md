# CORE GILBARCOENCORE500
Version 32
1. feat: Se activa ruta para enviar lecturas a la ruta	http://localhost:8019/api/recibirLecturas
2. feat: Se guardan las transmisiones de cierre para garantizar entregas
3. feat: Se activa ControllerSync con el fin de transmitir la información retenida
4. feat: Se normaliza el serverWs como servicio en hilo secundario tal como en encore 500
5. feat: Se registran los totalizadores iniciales en jornada inventario controller 
6. fix:  Se corrige asignar ventas de otro turno
7. feat: Se bloquea las mangueras con cierre de turno

Version 33
1. fix:  Se eliminan las jornadas cuando es mas de uno
2. fix:  Se agrega un margen de 0.5 para cuando se vende aire

Version 34 [Solo aplica a encore500 y encore300]
1. feat: Se agrega el parametro [factor_predeterminacion_volumen] default: 100
2. feat: Programa exige que tenga un valor coherente 1, 10, 100, 1000
3. feat: Multiplica por el [factor_predeterminacion_volumen] al momento de autorizar
4. fix:  Se prioriza las transmisiones tipo [CIERRE_JORNADA] 
