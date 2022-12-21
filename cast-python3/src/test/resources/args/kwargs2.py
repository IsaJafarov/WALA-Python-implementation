def func2(f3, arg3):
    f3(arg3)
    return arg3


def func3(arg3):
    return arg3


kw_args = {"f3": func3, "arg3": "a3"}
# 29   putfield v47.< PythonLoader, LRoot, arg3, <PythonLoader,LRoot> > = v50:#a3kwargs2.py [11:10] -> [11:37] [47=[kw_args]]
# 31   fieldref v53:#null.v54:#0 = v47 = v47   kwargs2.py [1:0] -> [1:0] [47=[kw_args]]
# 32   v52 = invokeFunction < PythonLoader, LCodeBody, do()LRoot; > v43 @32 exception:v55 null:47kwargs2.py [13:0] -> [13:16] [43=[func2]47=[kw_args]]
func2(**kw_args)
