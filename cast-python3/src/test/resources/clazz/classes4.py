def fa(x):
    return x+1

class Ctor():
    def __init__(self):
        pass

    def get(self, x):
        x()

c = Ctor()
c.get(fa)
