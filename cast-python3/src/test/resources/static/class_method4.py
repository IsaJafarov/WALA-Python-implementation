class A:
    class B:
        @classmethod
        def func4(cls):
            cls.func5()
            return "func4"
        @classmethod
        def func5(cls):
            return "func5"

A.B.func4()
