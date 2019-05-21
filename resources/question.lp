question('how many utensils are there on the table ?', 1).
question('how many utensils are there on the table ?', certain, X1) :- _mod(utensil, many),_property(E1, be, _by, S1),_property(E1, be, on, table),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, _, O1),total_element(utensil, X1).
question('how many utensils are there on the table ?', certain, X1) :- _mod(utensil, many),_property(E1, be, on, table),_relation(S1, E1, _clause),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, _, _),total_element(utensil, X1).
question('how many utensils are there on the table ?', certain, X1) :- _mod(utensil, many),_property(E1, be, on, table),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, S1, O1),total_element(utensil, X1).
question('how many utensils are there on the table ?', likely, X1) :- _property(E1, be, _by, S1),_property(E1, be, on, table),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, _, O1),total_element(utensil, X1).
question('how many utensils are there on the table ?', likely, X1) :- _property(E1, be, on, table),_relation(S1, E1, _clause),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, _, _),total_element(utensil, X1).
question('how many utensils are there on the table ?', likely, X1) :- _property(E1, be, on, table),_similar(many_utensil, S1),_similar(utensil, S1),event(E1, be, S1, O1),total_element(utensil, X1).
question('how many utensils are there on the table ?', possible, X1) :- total_element(utensil, X1).
?- query(1,Question,answer( Answer, Confidence_Level)).