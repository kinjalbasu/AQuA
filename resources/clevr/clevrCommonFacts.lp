similar(X,Y) :- is_similar(X,Y).
similar(X,Y) :- is_similar(Y,X).

is_property(red,color).
is_property(green,color).
is_property(cube,shape).

is_similar(block,cube).
is_similar(ball,sphere).
