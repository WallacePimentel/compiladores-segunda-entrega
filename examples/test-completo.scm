;; ==========================================
;; DEFINIÇÕES GLOBAIS (Resolve os erros de escopo)
;; ==========================================
(define iniciar (lambda () (print "Iniciando o programa...")))
(define x 10)
(define y 20)
(define testa-algo (lambda () #t))
(define processa-resultado (lambda (val) "Processado com sucesso!"))

;; ==========================================
;; CÓDIGO ORIGINAL DO TESTE
;; ==========================================
(define limite 100)

(set! limite 200)

(+ 10 20 limite)

(iniciar)

(if (> limite 50) 
    "maior" 
    "menor")

(if #t 10)

(lambda (x y) 
  (+ x y))

(lambda argumentos 
  (print argumentos))

(lambda (a b . resto) 
  (+ a b))

(let ((a 1)
      (b 2))
  (+ a b))

(let loop ((x 10))
  (if (> x 0)
      (loop (- x 1))
      x))

(let* ((x 1)
       (y (+ x 1)))
  y)

(letrec ((f (lambda () 1)))
  (f))

(begin
  (set! x 1)
  (set! y 2)
  (+ x y))

(and #t #f 
     (or #t #f))

(cond 
  ((> x 10) "maior")
  ((= x 10) "igual")
  (else "menor"))

(cond 
  ((testa-algo) => processa-resultado))

(do ((i 0 (+ i 1))
     (j 10 (- j 1)))
    ((= i 10) j)
  (print i)
  (print j))

(delay (+ 10 20))