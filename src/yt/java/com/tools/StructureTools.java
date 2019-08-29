package yt.java.com.tools;

import static java.lang.reflect.Array.newInstance;

import java.lang.reflect.InvocationTargetException;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class StructureTools {

	public static <T extends Structure> T Pointer2Structure(Pointer pParam, Class<T> res)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		T Param;
		Param = res.getDeclaredConstructor().newInstance();
		Pointer facePointer = Param.getPointer();
		facePointer.write(0, pParam.getByteArray(0, Param.size()), 0, Param.size());
		Param.read();
		return Param;
	}
	
	public static <T extends Structure> T[] Pointer2StructureList(Pointer pt, Class<T> res, int listSize)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		T[] paramLisT = (T[]) newInstance(res, listSize);
		for (int i = 0; i < listSize; i++) {
			paramLisT[i] = res.getDeclaredConstructor().newInstance();
			Pointer ptParam = paramLisT[i].getPointer();
			ptParam.write(0, pt.getByteArray(paramLisT[i].size()*i, paramLisT[i].size()), 0, paramLisT[i].size());
			paramLisT[i].read();
		}
		return paramLisT;
	}
}
