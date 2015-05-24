package tonivade.db.command.impl;

import static tonivade.db.data.DatabaseValue.string;
import tonivade.db.command.ICommand;
import tonivade.db.command.IRequest;
import tonivade.db.command.IResponse;
import tonivade.db.command.annotation.ParamLength;
import tonivade.db.command.annotation.ParamType;
import tonivade.db.data.DataType;
import tonivade.db.data.DatabaseValue;
import tonivade.db.data.IDatabase;

@ParamLength(1)
@ParamType(DataType.STRING)
public class StringLengthCommand implements ICommand {

    @Override
    public void execute(IDatabase db, IRequest request, IResponse response) {
        DatabaseValue value = db.getOrDefault(request.getParam(0), string(""));
        String string = value.getValue();
        response.addInt(string.length());
    }

}