// EXAMPLES FOR CORRECT INTERPROCEDURAL TYPESTATE.

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?


  void ok1() {
    // Constructor will trigger MARK rule
    Botan2 p2 = Botan2_new();

    Botan2_create(p2);

    // Aliasing: Operations on p3 are now equal to p2
    //Botan2 p3 = p2;

    Botan2_init(p2);

    // Continue in other function + alias to p4
    Botan2_start(p2);

    //cout << "Some irrelevant stmt\n";
    Botan2_process(p2);
    Botan2_process(p2);

    Botan2_process(p2);

    Botan2_finish(p2);  // Finish on p4 alias

    //cout << "Worked.\n";
  }

  Botan2 someFunction(Botan2 x) {
    // The missing start() is here
    Botan2_start(x);
    return x;
  }
