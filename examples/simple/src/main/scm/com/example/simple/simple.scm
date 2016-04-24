(define-library (com example simple)
  (import
    ;; Android
    (class android.app Activity)
    (class android.os Bundle)
    ;; Scheme
    (scheme base)
    (kawa base))
  (begin
    (define-simple-class SimpleActivity (Activity))))
