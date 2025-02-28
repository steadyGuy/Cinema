import { Box, Button, Container, Typography } from '@mui/material';
import { useFormik } from 'formik';
import React from 'react'
import { useAppDispatch, useAppSelector } from '../../globalStore/hooks';
import { loginUser, registerUser, selectAuth } from '../../globalStore/slices/authSlice';
import { validationLoginSchema, validationRegisterSchema } from '../../utils/yup';
import Link from 'next/link'
import Input from '../Input';
import SubmitBtn from '../SubmitBtn';
import Loader from '../Loader';
import { useRouter } from 'next/router';

const RegisterForm = () => {

  const dispatch = useAppDispatch();
  const auth = useAppSelector(selectAuth);
  const router = useRouter();

  const styles = {
    width: 320,
    marginTop: 5,
  };

  const formik = useFormik({
    initialValues: {
      email: '',
      password: '',
      firstname: '',
      lastname: '',
      repeatPassword: '',
    },
    validationSchema: validationRegisterSchema,
    onSubmit: async (form) => {
      dispatch(registerUser(form)).unwrap().then(() => {
        router.push('/');
      });
    },
  });

  if (auth.loading === 'pending') {
    return <Loader />;
  }

  return (
    <Container>
      <Box sx={{ display: 'flex', alignItems: 'center', flexDirection: 'column', width: '100%' }}>
        <Typography variant="h2" sx={{ fontWeight: 'bold' }} align="center">
          Register to view data
        </Typography>
        <Typography variant="subtitle1" sx={{ fontWeight: 'lighter' }} align="center">
          Choose a lot of different moovies, book a tickets and do whatever you want
        </Typography>
        <form onSubmit={formik.handleSubmit}>
          <Input formik={formik} label="Firstname" name="firstname" inputStyles={styles} />
          <Input formik={formik} label="Lastname" name="lastname" inputStyles={styles} />
          <Input formik={formik} label="Email" name="email" inputStyles={styles} />
          <Input
            formik={formik}
            label="Password"
            type="password"
            name="password"
            inputStyles={styles}
          />
          <Input
            formik={formik}
            label="Repeat Password"
            type="password"
            name="repeatPassword"
            inputStyles={styles}
          />
          <SubmitBtn title="Register" />
        </form>
        <Box display="flex" justifyContent="space-between">
          <Link href="/auth/login" passHref>
            Alreade have an account? Login
          </Link>
        </Box>
      </Box>
    </Container>
  )
}

export default RegisterForm
