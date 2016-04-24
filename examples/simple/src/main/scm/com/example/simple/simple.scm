(define-library (com example simple simple)
  (import
    ;; Android
    (class android.app Activity)
    (class android.os Bundle)
    ;; Scheme
    (scheme base)
    (kawa base))
  (export SimpleActivity)
  (begin
    (define-simple-class SimpleActivity (Activity))))
